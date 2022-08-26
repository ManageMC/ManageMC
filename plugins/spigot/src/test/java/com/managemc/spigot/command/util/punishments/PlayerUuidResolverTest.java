package com.managemc.spigot.command.util.punishments;

import com.managemc.spigot.command.util.punishments.model.ResolvedPlayer;
import com.managemc.spigot.testutil.TestConstants;
import com.managemc.spigot.testutil.TestMocks;
import com.managemc.spigot.util.HttpRequestMaker;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.bukkit.entity.Player;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;

import java.util.UUID;

public class PlayerUuidResolverTest {

  public static final String USERNAME = TestConstants.JACOB_USERNAME;
  public static final UUID JACOB_UUID = TestConstants.JACOB_UUID;

  private static final VerificationMode NEVER = Mockito.never();
  private static final VerificationMode ONCE = Mockito.times(1);

  private TestMocks mocks;
  private PlayerUuidResolver resolver;

  @Before
  public void setup() {
    mocks = new TestMocks();
    resolver = new PlayerUuidResolver(mocks.getLogging(), mocks.getBukkitWrapper(), new HttpRequestMaker());
  }

  @Test
  public void onlinePlayer() {
    Player player = Mockito.mock(Player.class);
    Mockito.when(player.getUniqueId()).thenReturn(JACOB_UUID);
    Mockito.when(mocks.getBukkitWrapper().getOnlineOrRecentlyOnlinePlayer(USERNAME))
        .thenReturn(player);
    ResolvedPlayer resolvedPlayer = resolver.resolvePlayerPotentiallyBlocking(USERNAME);

    Assert.assertEquals(JACOB_UUID, resolvedPlayer.getUuid());
    Assert.assertEquals(ResolvedPlayer.Status.ONLINE_RECENTLY, resolvedPlayer.getStatusCode());
  }

  @Test
  public void resolvesOnePlayer() {
    ResolvedPlayer player = resolver.resolvePlayerPotentiallyBlocking(USERNAME);

    Assert.assertEquals(JACOB_UUID, player.getUuid());
    Assert.assertEquals(ResolvedPlayer.Status.HTTP_OK, player.getStatusCode());
  }

  @Test
  public void resolvesAnotherPlayer() {
    ResolvedPlayer player = resolver.resolvePlayerPotentiallyBlocking(TestConstants.PHYLLIS_USERNAME);

    Assert.assertEquals(TestConstants.PHYLLIS_UUID, player.getUuid());
    Assert.assertEquals(ResolvedPlayer.Status.HTTP_OK, player.getStatusCode());
  }

  @Test
  public void notFound() {
    ResolvedPlayer player = resolver.resolvePlayerPotentiallyBlocking("n_os_uch_play_er");

    Assert.assertNull(player.getUuid());
    Assert.assertEquals(ResolvedPlayer.Status.HTTP_NOT_FOUND, player.getStatusCode());
    Mockito.verify(mocks.getLogging(), NEVER).logWarning(Mockito.any());
    Mockito.verify(mocks.getLogging(), NEVER).logStackTrace(Mockito.any());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void rateLimitExceeded() throws Exception {
    HttpRequestMaker httpRequestMaker = Mockito.mock(HttpRequestMaker.class);
    HttpResponse<String> response = (HttpResponse<String>) Mockito.mock(HttpResponse.class);
    Mockito.when(response.getStatus()).thenReturn(PlayerUuidResolver.RATE_LIMIT_EXCEEDED_CODE);
    Mockito.when(httpRequestMaker.get(Mockito.any())).thenReturn(response);
    ResolvedPlayer player = new PlayerUuidResolver(mocks.getLogging(), mocks.getBukkitWrapper(), httpRequestMaker)
        .resolvePlayerPotentiallyBlocking(USERNAME);

    Assert.assertNull(player.getUuid());
    Assert.assertEquals(ResolvedPlayer.Status.HTTP_RATE_LIMIT_EXCEEDED, player.getStatusCode());
    Mockito.verify(mocks.getLogging(), NEVER).logWarning(Mockito.any());
    Mockito.verify(mocks.getLogging(), NEVER).logStackTrace(Mockito.any());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void unhandledStatusCode() throws Exception {
    HttpRequestMaker httpRequestMaker = Mockito.mock(HttpRequestMaker.class);
    HttpResponse<String> response = (HttpResponse<String>) Mockito.mock(HttpResponse.class);
    Mockito.when(response.getStatus()).thenReturn(500);
    Mockito.when(response.getBody()).thenReturn("{\"foo\": \"bar\"}");
    Mockito.when(httpRequestMaker.get(Mockito.any())).thenReturn(response);
    ResolvedPlayer player = new PlayerUuidResolver(mocks.getLogging(), mocks.getBukkitWrapper(), httpRequestMaker)
        .resolvePlayerPotentiallyBlocking(USERNAME);

    Assert.assertNull(player.getUuid());
    Assert.assertEquals(ResolvedPlayer.Status.HTTP_UNEXPECTED, player.getStatusCode());
    Mockito.verify(mocks.getLogging(), ONCE).logWarning("Unexpected HTTP code from Mojang API\n  code: 500\n  Player: JacobCrofts");
    Mockito.verify(mocks.getLogging(), NEVER).logStackTrace(Mockito.any());
  }

  @Test
  public void unirestException() throws Exception {
    UnirestException exception = new UnirestException("oops!");
    HttpRequestMaker httpRequestMaker = Mockito.mock(HttpRequestMaker.class);
    Mockito.when(httpRequestMaker.get(Mockito.any())).thenThrow(exception);
    ResolvedPlayer player = new PlayerUuidResolver(mocks.getLogging(), mocks.getBukkitWrapper(), httpRequestMaker)
        .resolvePlayerPotentiallyBlocking(USERNAME);

    Assert.assertNull(player.getUuid());
    Assert.assertEquals(ResolvedPlayer.Status.CLIENT_EXCEPTION, player.getStatusCode());
    Mockito.verify(mocks.getLogging(), NEVER).logWarning(Mockito.any());
    Mockito.verify(mocks.getLogging(), ONCE).logStackTrace(Mockito.any());
  }
}
