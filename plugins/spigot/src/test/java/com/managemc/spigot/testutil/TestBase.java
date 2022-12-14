package com.managemc.spigot.testutil;

import com.managemc.api.ApiException;
import com.managemc.api.wrapper.ClientProvider;
import com.managemc.api.wrapper.client.PlayerClient;
import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.spigot.command.util.punishments.PlayerUuidResolver;
import com.managemc.spigot.command.util.punishments.model.ResolvedPlayer;
import com.managemc.spigot.config.SpigotPluginConfigTest;
import com.managemc.spigot.state.model.MuteData;
import com.managemc.spigot.util.permissions.Permission;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.verification.VerificationMode;
import org.openapitools.client.model.BanOrMute;
import org.openapitools.client.model.PunishmentVisibility;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.awaitility.Awaitility.await;

@RunWith(MockitoJUnitRunner.Silent.class)
public abstract class TestBase {

  protected static final VerificationMode NEVER = Mockito.never();
  protected static final VerificationMode ONCE = Mockito.times(1);
  protected static final VerificationMode TWICE = Mockito.times(2);

  protected static final String JACOB_USERNAME = TestConstants.JACOB_USERNAME;
  protected static final UUID JACOB_UUID = TestConstants.JACOB_UUID;
  protected static final String PHYLLIS_USERNAME = TestConstants.PHYLLIS_USERNAME;
  protected static final UUID PHYLLIS_UUID = TestConstants.PHYLLIS_UUID;
  protected static final String PLS_USERNAME = "pls";
  protected static final UUID PLS_UUID = UUID.fromString("5de82748-dbea-4db9-9027-2edfe6d861e3");

  protected SpigotPluginConfigTest config;
  @Mock
  protected CommandSender sender;
  @Mock
  protected Command command;
  @Mock
  private PlayerUuidResolver uuidResolver;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  protected ClientProvider mockClients;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  protected PlayerClient playerClient;

  @Before
  public final void abstractBefore() throws ApiException {
    config = new SpigotPluginConfigTest(new TestMocks(), TestWebClients.CLIENT_PROVIDER);
    config.setUuidResolver(uuidResolver);
    Mockito.when(mockClients.player(Mockito.any())).thenReturn(playerClient);

    // make sure that we have connected to the service at least once
    // the first request takes a while to execute
    // subsequent requests are fast
    // this ensures that we have made the "slow" request before any tests are run
    // we also need to make sure we only run this once for the entire test suite
    if (!(ClientProvider.COMPLETED_REQUESTS.get() > 0)) {
      TestWebClients.CLIENT_PROVIDER.externalServer().getPingApi().ping();
    }
  }

  protected void awaitWebServiceResponse(VoidFunctionPossiblyThrowingException function) {
    awaitWebServiceResponse(function, 1);
  }

  protected void awaitWebServiceResponse(VoidFunctionPossiblyThrowingException function, int totalRequests) {
    long completedRequests = ClientProvider.COMPLETED_REQUESTS.get() + totalRequests - 1;
    try {
      function.apply();
    } catch (Exception e) {
      e.printStackTrace();
    }
    await().atMost(20, TimeUnit.SECONDS).until(() -> ClientProvider.COMPLETED_REQUESTS.get() > completedRequests);
  }

  protected void awaitAsyncCommand(VoidFunctionPossiblyThrowingException function) {
    long completedRequests = CommandExecutorAsync.COMPLETED_COMMANDS.get();
    try {
      function.apply();
    } catch (Exception e) {
      e.printStackTrace();
    }
    await().atMost(20, TimeUnit.SECONDS).until(() -> CommandExecutorAsync.COMPLETED_COMMANDS.get() > completedRequests);
  }

  protected void stubOnlinePlayers(String... usernames) {
    Set<Player> players = new HashSet<>();

    for (String name : usernames) {
      Player player = Mockito.mock(Player.class);
      Mockito.when(player.getName()).thenReturn(name);
      Mockito.when(player.isOnline()).thenReturn(true);
      players.add(player);
    }

    Mockito.when(config.getBukkitWrapper().getOnlinePlayers()).thenReturn(players);
  }

  protected void stubResolvedPlayer(String username, UUID uuid, ResolvedPlayer.Status status) {
    ResolvedPlayer resolvedPlayer = ResolvedPlayer.builder()
        .uuid(uuid)
        .statusCode(status)
        .build();
    Mockito
        .when(uuidResolver.resolvePlayerPotentiallyBlocking(username))
        .thenReturn(resolvedPlayer);
  }

  protected Player newStubbedPlayer(String username, UUID uuid, boolean online) {
    Player player = Mockito.mock(Player.class);
    Mockito.when(player.getName()).thenReturn(username);
    Mockito.when(player.getUniqueId()).thenReturn(uuid);
    Mockito.when(player.isOnline()).thenReturn(online);

    if (online) {
      stubResolvedPlayer(username, uuid, ResolvedPlayer.Status.ONLINE_RECENTLY);
      Mockito.when(config.getBukkitWrapper().getOnlinePlayer(username)).thenReturn(player);

      Mockito
          .when(config.getBukkitWrapper().getOnlineOrRecentlyOnlinePlayer(username))
          .thenReturn(player);

      Mockito
          .when(config.getBukkitWrapper().getOnlinePlayer(uuid))
          .thenReturn(player);

      Set<Player> oldOnlinePlayers = config.getBukkitWrapper().getOnlinePlayers();
      if (oldOnlinePlayers == null) {
        Mockito
            .when(config.getBukkitWrapper().getOnlinePlayers())
            .thenReturn(Collections.singleton(player));
      } else {
        Set<UUID> onlineUuids = oldOnlinePlayers.stream().map(Player::getUniqueId).collect(Collectors.toSet());
        if (!onlineUuids.contains(uuid)) {
          oldOnlinePlayers.add(player);
          Mockito.when(config.getBukkitWrapper().getOnlinePlayers())
              .thenReturn(oldOnlinePlayers);
        }
      }
    } else {
      Mockito.when(config.getBukkitWrapper().getOnlinePlayer(username)).thenReturn(null);
    }

    return player;
  }

  protected void stubPlayerSenderWithPermissions(Permission... permissions) {
    stubPlayerSenderWithPermissions(UUID.randomUUID(), permissions);
  }

  protected void stubPlayerSenderWithPermissions(UUID uuid, Permission... permissions) {
    sender = Mockito.mock(Player.class);
    Mockito.when(((Player) sender).getUniqueId()).thenReturn(uuid);
    Mockito.when(sender.hasPermission(Mockito.anyString())).thenReturn(false);

    for (Permission p : permissions) {
      Mockito.when(sender.hasPermission(p.getName())).thenReturn(true);
    }
  }

  protected void expectAbort(String message) {
    expectSingleMessage(ChatColor.RED + message);
  }

  protected void expectSingleMessage(String message) {
    Mockito.verify(sender).sendMessage(message);
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
  }

  protected void issueMute(boolean shadow, String reason, Long expiresAt) {
    BanOrMute mute = new BanOrMute()
        .visibility(shadow ? PunishmentVisibility.SHADOW : PunishmentVisibility.PUBLIC)
        .reason(reason)
        .duration(expiresAt == null ? null : expiresAt - System.currentTimeMillis());

    MuteData muteData = new MuteData(TestConstants.JACOB_UUID, mute);
    config.getMuteManager().setMuted(muteData);
  }

  @FunctionalInterface
  protected interface VoidFunctionPossiblyThrowingException {
    void apply() throws Exception;
  }
}
