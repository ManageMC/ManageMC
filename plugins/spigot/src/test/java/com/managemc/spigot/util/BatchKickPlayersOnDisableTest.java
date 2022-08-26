package com.managemc.spigot.util;

import com.managemc.api.ApiException;
import com.managemc.plugins.bukkit.BukkitWrapper;
import com.managemc.plugins.logging.BukkitLogging;
import com.managemc.spigot.service.LoginLogoutService;
import org.bukkit.entity.Player;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RunWith(MockitoJUnitRunner.Silent.class)
public class BatchKickPlayersOnDisableTest {

  @Mock
  private BukkitLogging logging;
  @Mock
  private BukkitWrapper bukkitWrapper;
  @Mock
  private LoginLogoutService service;

  private Map<UUID, Integer> testData;
  private List<Player> players;

  @Before
  public void setup() throws ApiException {
    testData = new ConcurrentHashMap<>();
    players = new ArrayList<>();

    Mockito.when(bukkitWrapper.getOnlinePlayers()).thenReturn(new HashSet<>(players));
  }

  @Test
  public void success() throws ApiException {
    stubCalls();

    Assert.assertTrue(testData.values().stream().allMatch(d -> d == 0));
    new BatchKickPlayersOnDisable(logging, bukkitWrapper, service).run();
    Assert.assertTrue(testData.values().stream().allMatch(d -> d == 1));
  }

  @Test
  public void partialSuccess() throws ApiException {
    stubCalls(5, 26, 77);

    Assert.assertTrue(testData.values().stream().allMatch(d -> d == 0));

    new BatchKickPlayersOnDisable(logging, bukkitWrapper, service).run();

    Mockito.verify(logging, Mockito.times(3)).logStackTrace(Mockito.any());

    Assert.assertEquals(3, testData.values().stream().filter(d -> d == 0).count());
    Assert.assertEquals(97, testData.values().stream().filter(d -> d == 1).count());
  }

  private void stubCalls(Integer... indicesThrowingErrors) throws ApiException {
    List<Integer> badIndices = Arrays.asList(indicesThrowingErrors);

    for (int i = 0; i < 100; i++) {
      Player player = Mockito.mock(Player.class);
      UUID uuid = UUID.randomUUID();
      Mockito.when(player.getUniqueId()).thenReturn(uuid);
      players.add(player);
      testData.put(uuid, 0);

      if (badIndices.contains(i)) {
        Mockito
            .doThrow(new ApiException())
            .when(service).onLogout(uuid);
      } else {
        Mockito
            .doAnswer((method) -> {
              // sleep just long enough to prove the method is blocking
              Thread.sleep(20);
              return testData.put(uuid, testData.get(uuid) + 1);
            })
            .when(service).onLogout(uuid);
      }

      Mockito.when(bukkitWrapper.getOnlinePlayers()).thenReturn(new HashSet<>(players));
    }
  }
}
