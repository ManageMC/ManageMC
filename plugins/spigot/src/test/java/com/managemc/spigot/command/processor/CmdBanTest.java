package com.managemc.spigot.command.processor;

import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.spigot.command.util.CommandAssertions;
import com.managemc.spigot.command.util.punishments.model.ResolvedPlayer;
import com.managemc.spigot.testutil.TestBase;
import com.managemc.spigot.testutil.TestConstants;
import com.managemc.spigot.util.KickMessages;
import com.managemc.spigot.util.TextConstants;
import com.managemc.spigot.util.permissions.Permission;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.UUID;

public class CmdBanTest extends TestBase {

  private Player offender;

  @Before
  public void setup() {
    offender = newStubbedPlayer(JACOB_USERNAME, JACOB_UUID, true);
  }

  @Test
  public void notEnoughArgs() {
    onCommand(false);
    expectAbort(CommandAssertions.WRONG_NUM_ARGS);
  }

  @Test
  public void syntacticallyInvalidUsername() {
    onCommand(false, "not-a-valid-username");
    expectAbort("Invalid username not-a-valid-username");
  }

  @Test
  public void invalidDurationString() {
    onCommand(false, JACOB_USERNAME, "cheating", "--duration=24");
    expectAbort("Invalid duration \"24\" (example: --duration=24h)");
  }

  @Test
  public void emptyDurationString() {
    onCommand(false, JACOB_USERNAME, "cheating", "--duration=");
    expectAbort("Invalid duration \"\" (example: --duration=24h)");
  }

  @Test
  public void online_noReason() {
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME));

    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully banned JacobCrofts"));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());

    Assert.assertEquals(1, config.getPardonablePunishmentData().getPunishmentIdsKnownToSender(sender).size());
    String externalId = config.getPardonablePunishmentData().getPunishmentIdsKnownToSender(sender).get(0);
    Assert.assertEquals("ban", externalId.split("-")[0]);
  }

  @Test
  public void online_withReason_oneWord() {
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME, "shh"));

    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully banned JacobCrofts"));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
    Mockito.verify(offender).kickPlayer(Mockito.contains("reason: shh"));
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
  }

  @Test
  public void online_withReason_twoWords() {
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME, "\"being", "annoying", "\""));

    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully banned JacobCrofts"));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
    Mockito.verify(offender).kickPlayer(Mockito.contains("reason: being annoying"));
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
  }

  @Test
  public void offline_onlineRecently() {
    stubOfflineOffender(ResolvedPlayer.Status.ONLINE_RECENTLY);
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME));

    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully banned JacobCrofts"));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender, NEVER).kickPlayer(Mockito.any());
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
  }

  @Test
  public void offline() {
    stubOfflineOffender(ResolvedPlayer.Status.HTTP_OK);
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME));

    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully banned JacobCrofts"));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender, NEVER).kickPlayer(Mockito.any());
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
  }

  @Test
  public void offline_notFound() {
    stubOfflineOffender(ResolvedPlayer.Status.HTTP_NOT_FOUND);
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME));

    expectAbort(CommandAssertions.NOT_FOUND_MSG);
    Mockito.verify(offender, NEVER).kickPlayer(Mockito.any());
  }

  @Test
  public void offline_rateLimitExceeded() {
    stubOfflineOffender(ResolvedPlayer.Status.HTTP_RATE_LIMIT_EXCEEDED);
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME));

    expectAbort(CommandAssertions.MOJANG_RATE_LIMIT_MSG);
    Mockito.verify(offender, NEVER).kickPlayer(Mockito.any());
  }

  @Test
  public void offline_unexpectedHttpStatus() {
    stubOfflineOffender(ResolvedPlayer.Status.HTTP_UNEXPECTED);
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME));

    Mockito.verify(sender).sendMessage(ChatColor.DARK_RED + "Unexpected plugin exception");
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender, NEVER).kickPlayer(Mockito.any());
    Mockito.verify(config.getLogging(), ONCE).logStackTrace(Mockito.any());
  }

  @Test
  public void offline_uuidResolverClientError() {
    stubOfflineOffender(ResolvedPlayer.Status.CLIENT_EXCEPTION);
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME));

    Mockito.verify(sender).sendMessage(ChatColor.DARK_RED + "Unexpected plugin exception");
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender, NEVER).kickPlayer(Mockito.any());
    Mockito.verify(config.getLogging(), ONCE).logStackTrace(Mockito.any());
  }

  @Test
  public void whenSuccessful_withAPlayerIssuer_shouldDisplayMessagesCorrectly() {
    stubPlayerSenderWithPermissions(JACOB_UUID, Permission.OWNER);
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME));

    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully banned JacobCrofts"));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
    Mockito.verify(offender).kickPlayer(Mockito.contains("reason: " + ChatColor.RESET));
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());

    Assert.assertEquals(1, config.getPardonablePunishmentData().getPunishmentIdsKnownToSender(sender).size());
    String externalId = config.getPardonablePunishmentData().getPunishmentIdsKnownToSender(sender).get(0);
    Assert.assertEquals("ban", externalId.split("-")[0]);
  }

  @Test
  public void playerSender_adminPermission() {
    stubPlayerSenderWithPermissions(JACOB_UUID, Permission.ADMIN);
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME, "--shadow"));

    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully banned JacobCrofts"));
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.SHADOW_BAN_MESSAGE));
  }

  @Test
  public void playerSender_banWebPermission() {
    stubPlayerSenderWithPermissions(JACOB_UUID, Permission.BAN_WEB);
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME, "--duration=24h"));

    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully banned JacobCrofts"));
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
  }

  @Test
  public void playerSender_banInGamePermission() {
    stubPlayerSenderWithPermissions(JACOB_UUID, Permission.BAN_IN_GAME);
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME, "--duration=24h"));

    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully banned JacobCrofts"));
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
  }

  @Test
  public void playerSender_mediumBanTooLong() {
    stubPlayerSenderWithPermissions(JACOB_UUID, Permission.BAN_IN_GAME);
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME, "--duration=24h1m"));

    expectAbort(CommandAssertions.PUNISHMENT_TOO_LONG_MSG);
    Mockito.verify(offender, NEVER).kickPlayer(Mockito.anyString());
  }

  @Test
  public void playerSender_mediumBanNotTooLong() {
    stubPlayerSenderWithPermissions(JACOB_UUID, Permission.BAN_IN_GAME, Permission.PUNISHMENTS_LENGTH_MED);
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME, "--duration=24h1m"));

    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully banned JacobCrofts"));
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
  }

  @Test
  public void playerSender_longBanTooLong() {
    stubPlayerSenderWithPermissions(JACOB_UUID, Permission.BAN_IN_GAME, Permission.PUNISHMENTS_LENGTH_MED);
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME, "--duration=7d1m"));

    expectAbort(CommandAssertions.PUNISHMENT_TOO_LONG_MSG);
    Mockito.verify(offender, NEVER).kickPlayer(Mockito.anyString());
  }

  @Test
  public void playerSender_longBanNotTooLong() {
    stubPlayerSenderWithPermissions(JACOB_UUID, Permission.BAN_IN_GAME, Permission.PUNISHMENTS_LENGTH_LONG);
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME, "--duration=7d1m"));

    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully banned JacobCrofts"));
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
  }

  @Test
  public void playerSender_permanentBanTooLong() {
    stubPlayerSenderWithPermissions(JACOB_UUID, Permission.BAN_WEB, Permission.PUNISHMENTS_LENGTH_LONG);
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME));

    expectAbort(CommandAssertions.NO_PERMANENT_PUNISHMENTS_MSG);
    Mockito.verify(offender, NEVER).kickPlayer(Mockito.anyString());
  }

  @Test
  public void playerSender_permanentBanNotTooLong() {
    stubPlayerSenderWithPermissions(JACOB_UUID, Permission.BAN_WEB, Permission.PUNISHMENTS_LENGTH_PERM);
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME));

    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully banned JacobCrofts"));
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
  }

  @Test
  public void playerSender_noPermsForShadow() {
    stubPlayerSenderWithPermissions(JACOB_UUID, Permission.BAN_WEB, Permission.PUNISHMENTS_LENGTH_PERM);
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME, "--shadow"));

    expectAbort(CommandAssertions.NO_SHADOW_PUNISHMENTS_MSG);
    Mockito.verify(offender, NEVER).kickPlayer(Mockito.anyString());
  }

  @Test
  public void playerSender_noPerms() {
    stubPlayerSenderWithPermissions(JACOB_UUID);
    onCommand(true, JACOB_USERNAME);

    expectAbort(CommandAssertions.NO_PERMS_MESSAGE);
    Mockito.verify(offender, NEVER).kickPlayer(Mockito.anyString());
  }

  @Test
  public void playerSender_noPermsServerSide() {
    sender = Mockito.mock(Player.class);
    Mockito.when(((Player) sender).getUniqueId()).thenReturn(TestConstants.PHYLLIS_UUID);
    Mockito.when(sender.hasPermission(Mockito.anyString())).thenReturn(true);
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME));

    expectSingleMessage(TextConstants.INSUFFICIENT_PERMS);

    // we still kick the player, since they need to be removed from the server immediately
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
  }

  @Test
  public void playerNotFound() {
    offender = newStubbedPlayer(JACOB_USERNAME, UUID.randomUUID(), true);
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME));

    expectSingleMessage(String.format(TextConstants.Commands.Punishment.PUNISHMENT_404, JACOB_USERNAME));
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
  }

  @Test
  public void apiException() {
    // intentionally cause the call to fail by assigning a syntactically invalid UUID
    UUID invalidUuid = Mockito.mock(UUID.class);
    Mockito.when(invalidUuid.toString()).thenReturn("ayy lmao");
    offender = newStubbedPlayer(JACOB_USERNAME, invalidUuid, true);
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME));

    Mockito.verify(sender).sendMessage(ChatColor.DARK_RED + "Unexpected plugin exception");
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
    Mockito.verify(config.getLogging(), ONCE).logStackTrace(Mockito.any());
  }

  @Test
  public void successMessage_temp_withReason() {
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME, "cheating", "--duration=24h"));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully banned JacobCrofts"));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
  }

  @Test
  public void successMessage_local_temp_withReason() {
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME, "cheating", "--local", "--duration=24h"));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully banned JacobCrofts"));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
  }

  @Test
  public void successMessage_temp_noReason() {
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME, "--duration=24h"));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully banned JacobCrofts"));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
  }

  @Test
  public void successMessage_local_temp_noReason() {
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME, "--local", "--duration=24h"));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully banned JacobCrofts"));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
  }

  @Test
  public void visiblePunishmentBroadcastMessage_temp() {
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME, "cheating", "--broadcast", "--duration=24h"));
    Mockito.verify(config.getBukkitWrapper(), ONCE).broadcastMessage(ChatColor.GRAY + "JacobCrofts has been banned");
  }

  @Test
  public void visiblePunishmentBroadcastMessage() {
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME, "cheating", "--broadcast"));
    Mockito.verify(config.getBukkitWrapper(), ONCE).broadcastMessage(ChatColor.GRAY + "JacobCrofts has been banned");
  }

  @Test
  public void bothVisibleAndShadowFlags() {
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME, "cheating", "--broadcast", "--shadow"));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully banned JacobCrofts"));
    Mockito.verify(sender).sendMessage(TextConstants.Commands.BROADCAST_FLAG_IGNORED_MESSAGE);
    Mockito.verify(sender, TWICE).sendMessage(Mockito.anyString());
    Mockito.verify(config.getBukkitWrapper(), NEVER).broadcastMessage(Mockito.any());
  }

  @Test
  public void bothVisibleAndPrivateFlags() {
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME, "cheating", "--broadcast", "--private"));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully banned JacobCrofts"));
    Mockito.verify(sender).sendMessage(TextConstants.Commands.BROADCAST_FLAG_IGNORED_MESSAGE);
    Mockito.verify(sender, TWICE).sendMessage(Mockito.anyString());
    Mockito.verify(config.getBukkitWrapper(), NEVER).broadcastMessage(Mockito.any());
  }

  @Test
  public void bothShadowAndPrivateFlags() {
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME, "cheating", "--shadow", "--private"));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully banned JacobCrofts"));
    Mockito.verify(sender).sendMessage(TextConstants.Commands.PRIVATE_FLAG_REDUNDANT_MESSAGE);
    Mockito.verify(sender, TWICE).sendMessage(Mockito.anyString());
    Mockito.verify(config.getBukkitWrapper(), NEVER).broadcastMessage(Mockito.any());
  }

  @Test
  public void visibleShadowAndPrivateFlags() {
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME, "cheating", "--broadcast", "--private", "--shadow"));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully banned JacobCrofts"));
    Mockito.verify(sender).sendMessage(TextConstants.Commands.BROADCAST_FLAG_IGNORED_MESSAGE);
    Mockito.verify(sender).sendMessage(TextConstants.Commands.PRIVATE_FLAG_REDUNDANT_MESSAGE);
    Mockito.verify(sender, Mockito.times(3)).sendMessage(Mockito.anyString());
    Mockito.verify(config.getBukkitWrapper(), NEVER).broadcastMessage(Mockito.any());
  }

  @Test
  public void successMessage_local_perm_withReason() {
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME, "cheating", "--local"));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully banned JacobCrofts"));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
  }

  @Test
  public void kickOffender_shadow() {
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME, "cheating", "--shadow"));
    Mockito.verify(offender).kickPlayer(KickMessages.SHADOW_BAN_MESSAGE);
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
  }


  private void stubOfflineOffender(ResolvedPlayer.Status httpStatus) {
    stubResolvedPlayer(offender.getName(), offender.getUniqueId(), httpStatus);
    Mockito.when(config.getBukkitWrapper().getOnlinePlayer(offender.getName())).thenReturn(null);
  }

  private void onCommand(boolean noSyntaxError, String... args) {
    CommandExecutorAsync cmd = new CmdBan(config);
    Assert.assertEquals(noSyntaxError, cmd.onCommand(sender, command, "", args));
  }
}
