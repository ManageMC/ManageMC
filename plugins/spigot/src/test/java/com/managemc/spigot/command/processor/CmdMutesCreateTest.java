package com.managemc.spigot.command.processor;

import com.managemc.spigot.command.util.CommandAssertions;
import com.managemc.spigot.command.util.CommandNodeMap;
import com.managemc.spigot.command.util.CommandUsage;
import com.managemc.spigot.command.util.punishments.model.ResolvedPlayer;
import com.managemc.spigot.state.model.MuteData;
import com.managemc.spigot.testutil.TestBase;
import com.managemc.spigot.testutil.TestConstants;
import com.managemc.spigot.util.TextConstants;
import com.managemc.spigot.util.TimeHelper;
import com.managemc.spigot.util.chat.formatter.MuteDataFormatter;
import com.managemc.spigot.util.permissions.Permission;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.UUID;

public class CmdMutesCreateTest extends TestBase {

  private static final String NULL = ChatColor.GRAY + "null";

  private Player offender;

  private String expectedMuteReason = null;
  private Long expectedMuteExpirationTime = null;
  private boolean expectedShadow = false;

  @Before
  public void setup() {
    offender = newStubbedPlayer(JACOB_USERNAME, JACOB_UUID, true);
  }

  @Test
  public void notEnoughArgs() {
    onCommand();
    assertSyntactic(CommandAssertions.WRONG_NUM_ARGS, CommandUsage.MUTES_CREATE);
  }

  @Test
  public void syntacticallyInvalidUsername() {
    onCommand("not-a-valid-username");
    assertSyntactic("Invalid username not-a-valid-username", CommandUsage.MUTES_CREATE);
  }

  @Test
  public void invalidDurationString() {
    onCommand(JACOB_USERNAME, "cheating", "--duration=24");
    assertSyntactic("Invalid duration \"24\" (example: --duration=24h)", CommandUsage.MUTES_CREATE);
  }

  @Test
  public void emptyDurationString() {
    onCommand(JACOB_USERNAME, "cheating", "--duration=");
    assertSyntactic("Invalid duration \"\" (example: --duration=24h)", CommandUsage.MUTES_CREATE);
  }

  @Test
  public void online_noReason() {
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME));

    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully muted JacobCrofts"));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender).sendMessage(Mockito.contains(MuteDataFormatter.MUTED_HEADER));
    Mockito.verify(offender).sendMessage(Mockito.contains("reason: " + NULL));
    Mockito.verify(offender).sendMessage(Mockito.contains("expires: " + NULL));
    Mockito.verify(offender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());

    verifyMuteData();

    Assert.assertEquals(1, config.getPardonablePunishmentData().getPunishmentIdsKnownToSender(sender).size());
    String externalId = config.getPardonablePunishmentData().getPunishmentIdsKnownToSender(sender).get(0);
    Assert.assertEquals("mute", externalId.split("-")[0]);
  }

  @Test
  public void online_withReason_oneWord() {
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME, "shh"));

    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully muted JacobCrofts"));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender).sendMessage(Mockito.contains(MuteDataFormatter.MUTED_HEADER));
    Mockito.verify(offender).sendMessage(Mockito.contains("reason: " + ChatColor.BLUE + "shh"));
    Mockito.verify(offender).sendMessage(Mockito.contains("expires: " + NULL));
    Mockito.verify(offender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());

    expectedMuteReason = "shh";
    verifyMuteData();
  }

  @Test
  public void online_withReason_twoWords() {
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME, "\"being", "annoying", "\""));

    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully muted JacobCrofts"));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender).sendMessage(Mockito.contains(MuteDataFormatter.MUTED_HEADER));
    Mockito.verify(offender).sendMessage(Mockito.contains("reason: " + ChatColor.BLUE + "being annoying"));
    Mockito.verify(offender).sendMessage(Mockito.contains("expires: " + NULL));
    Mockito.verify(offender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());

    expectedMuteReason = "being annoying";
    verifyMuteData();
  }

  @Test
  public void offline_onlineRecently() {
    stubOfflineOffender(ResolvedPlayer.Status.ONLINE_RECENTLY);
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME));

    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully muted JacobCrofts"));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender, NEVER).sendMessage(Mockito.anyString());
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
  }

  @Test
  public void offline() {
    stubOfflineOffender(ResolvedPlayer.Status.HTTP_OK);
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME));

    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully muted JacobCrofts"));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender, NEVER).sendMessage(Mockito.anyString());
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
    Assert.assertNull(config.getMuteManager().getMuteData(JACOB_UUID));
  }

  @Test
  public void offline_notFound() {
    stubOfflineOffender(ResolvedPlayer.Status.HTTP_NOT_FOUND);
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME));

    Mockito.verify(sender).sendMessage(ChatColor.RED + CommandAssertions.NOT_FOUND_MSG);
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender, NEVER).sendMessage(Mockito.anyString());
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
    Assert.assertNull(config.getMuteManager().getMuteData(JACOB_UUID));
  }

  @Test
  public void offline_rateLimitExceeded() {
    stubOfflineOffender(ResolvedPlayer.Status.HTTP_RATE_LIMIT_EXCEEDED);
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME));

    Mockito.verify(sender).sendMessage(ChatColor.RED + CommandAssertions.MOJANG_RATE_LIMIT_MSG);
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender, NEVER).sendMessage(Mockito.anyString());
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
    Assert.assertNull(config.getMuteManager().getMuteData(JACOB_UUID));
  }

  @Test
  public void offline_unexpectedHttpStatus() {
    stubOfflineOffender(ResolvedPlayer.Status.HTTP_UNEXPECTED);
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME));

    Mockito.verify(sender).sendMessage(ChatColor.DARK_RED + "Unexpected plugin exception");
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender, NEVER).sendMessage(Mockito.anyString());
    Mockito.verify(config.getLogging(), ONCE).logStackTrace(Mockito.any());
    Assert.assertNull(config.getMuteManager().getMuteData(JACOB_UUID));
  }

  @Test
  public void offline_uuidResolverClientError() {
    stubOfflineOffender(ResolvedPlayer.Status.CLIENT_EXCEPTION);
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME));

    Mockito.verify(sender).sendMessage(ChatColor.DARK_RED + "Unexpected plugin exception");
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender, NEVER).sendMessage(Mockito.anyString());
    Mockito.verify(config.getLogging(), ONCE).logStackTrace(Mockito.any());
    Assert.assertNull(config.getMuteManager().getMuteData(JACOB_UUID));
  }

  @Test
  public void whenSuccessful_withAPlayerIssuer_shouldDisplayMessagesCorrectly() {
    stubPlayerSenderWithPermissions(JACOB_UUID, Permission.OWNER);
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME));

    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully muted JacobCrofts"));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender).sendMessage(Mockito.contains(MuteDataFormatter.MUTED_HEADER));
    Mockito.verify(offender).sendMessage(Mockito.contains("reason: " + NULL));
    Mockito.verify(offender).sendMessage(Mockito.contains("expires: " + NULL));
    Mockito.verify(offender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());

    verifyMuteData(true);

    Assert.assertEquals(1, config.getPardonablePunishmentData().getPunishmentIdsKnownToSender(sender).size());
    String externalId = config.getPardonablePunishmentData().getPunishmentIdsKnownToSender(sender).get(0);
    Assert.assertEquals("mute", externalId.split("-")[0]);
  }

  @Test
  public void playerSender_adminPermission() {
    stubPlayerSenderWithPermissions(JACOB_UUID, Permission.ADMIN);
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME, "--shadow"));

    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully muted JacobCrofts"));
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
    Mockito.verify(offender, NEVER).sendMessage(Mockito.anyString());

    expectedShadow = true;
    verifyMuteData(true);
  }

  @Test
  public void playerSender_muteWebPermission() {
    stubPlayerSenderWithPermissions(JACOB_UUID, Permission.MUTE_WEB);
    awaitAsyncCommand(() -> onCommand("--duration=24h", JACOB_USERNAME));

    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully muted JacobCrofts"));
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
    Mockito.verify(offender).sendMessage(Mockito.contains(MuteDataFormatter.MUTED_HEADER));

    expectedMuteExpirationTime = System.currentTimeMillis() + TimeHelper.ONE_DAY;
    verifyMuteData(true);
  }

  @Test
  public void playerSender_muteInGamePermission() {
    stubPlayerSenderWithPermissions(JACOB_UUID, Permission.MUTE_IN_GAME);
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME, "--duration=24h"));

    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully muted JacobCrofts"));
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
    Mockito.verify(offender).sendMessage(Mockito.contains(MuteDataFormatter.MUTED_HEADER));

    expectedMuteExpirationTime = System.currentTimeMillis() + TimeHelper.ONE_DAY;
    verifyMuteData(true);
  }

  @Test
  public void playerSender_mediumMuteTooLong() {
    stubPlayerSenderWithPermissions(JACOB_UUID, Permission.MUTE_IN_GAME);
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME, "--duration=24h1m"));

    Mockito.verify(sender).sendMessage(ChatColor.RED + CommandAssertions.PUNISHMENT_TOO_LONG_MSG);
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
    Mockito.verify(offender, NEVER).sendMessage(Mockito.anyString());

    Assert.assertNull(config.getMuteManager().getMuteData(JACOB_UUID));
  }

  @Test
  public void playerSender_mediumMuteNotTooLong() {
    stubPlayerSenderWithPermissions(JACOB_UUID, Permission.MUTE_IN_GAME, Permission.PUNISHMENTS_LENGTH_MED);
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME, "--duration=24h1m"));

    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully muted JacobCrofts"));
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
    Mockito.verify(offender).sendMessage(Mockito.contains(MuteDataFormatter.MUTED_HEADER));

    expectedMuteExpirationTime = System.currentTimeMillis() + TimeHelper.ONE_DAY + TimeHelper.ONE_MINUTE;
    verifyMuteData(true);
  }

  @Test
  public void playerSender_longMuteTooLong() {
    stubPlayerSenderWithPermissions(JACOB_UUID, Permission.MUTE_IN_GAME, Permission.PUNISHMENTS_LENGTH_MED);
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME, "--duration=7d1m"));

    Mockito.verify(sender).sendMessage(ChatColor.RED + CommandAssertions.PUNISHMENT_TOO_LONG_MSG);
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
    Mockito.verify(offender, NEVER).sendMessage(Mockito.anyString());

    Assert.assertNull(config.getMuteManager().getMuteData(JACOB_UUID));
  }

  @Test
  public void playerSender_longMuteNotTooLong() {
    stubPlayerSenderWithPermissions(JACOB_UUID, Permission.MUTE_IN_GAME, Permission.PUNISHMENTS_LENGTH_LONG);
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME, "--duration=7d1m"));

    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully muted JacobCrofts"));
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
    Mockito.verify(offender).sendMessage(Mockito.contains(MuteDataFormatter.MUTED_HEADER));

    expectedMuteExpirationTime = System.currentTimeMillis() + TimeHelper.ONE_DAY * 7 + TimeHelper.ONE_MINUTE;
    verifyMuteData(true);
  }

  @Test
  public void playerSender_permanentMuteTooLong() {
    stubPlayerSenderWithPermissions(JACOB_UUID, Permission.MUTE_WEB, Permission.PUNISHMENTS_LENGTH_LONG);
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME));

    Mockito.verify(sender).sendMessage(ChatColor.RED + CommandAssertions.NO_PERMANENT_PUNISHMENTS_MSG);
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
    Mockito.verify(offender, NEVER).sendMessage(Mockito.anyString());

    Assert.assertNull(config.getMuteManager().getMuteData(JACOB_UUID));
  }

  @Test
  public void playerSender_permanentMuteNotTooLong() {
    stubPlayerSenderWithPermissions(JACOB_UUID, Permission.MUTE_WEB, Permission.PUNISHMENTS_LENGTH_PERM);
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME));

    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully muted JacobCrofts"));
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
    Mockito.verify(offender).sendMessage(Mockito.contains(MuteDataFormatter.MUTED_HEADER));

    verifyMuteData(true);
  }

  @Test
  public void playerSender_noPermsForShadow() {
    stubPlayerSenderWithPermissions(JACOB_UUID, Permission.MUTE_WEB, Permission.PUNISHMENTS_LENGTH_PERM);
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME, "--shadow"));

    Mockito.verify(sender).sendMessage(ChatColor.RED + CommandAssertions.NO_SHADOW_PUNISHMENTS_MSG);
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
    Mockito.verify(offender, NEVER).sendMessage(Mockito.anyString());

    Assert.assertNull(config.getMuteManager().getMuteData(JACOB_UUID));
  }

  @Test
  public void playerSender_noPerms() {
    stubPlayerSenderWithPermissions(JACOB_UUID);
    onCommand(JACOB_USERNAME);

    Mockito.verify(sender).sendMessage(ChatColor.RED + CommandNodeMap.NO_PERMS_MSG);
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
    Mockito.verify(offender, NEVER).sendMessage(Mockito.anyString());

    Assert.assertNull(config.getMuteManager().getMuteData(JACOB_UUID));
  }

  @Test
  public void playerSender_noPermsServerSide() {
    sender = Mockito.mock(Player.class);
    Mockito.when(((Player) sender).getUniqueId()).thenReturn(TestConstants.PHYLLIS_UUID);
    Mockito.when(sender.hasPermission(Mockito.anyString())).thenReturn(true);
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME));

    Mockito.verify(sender).sendMessage(TextConstants.INSUFFICIENT_PERMS);
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
    Mockito.verify(offender, NEVER).sendMessage(Mockito.anyString());

    Assert.assertNull(config.getMuteManager().getMuteData(JACOB_UUID));
  }

  @Test
  public void playerNotFound() {
    offender = newStubbedPlayer(JACOB_USERNAME, UUID.randomUUID(), true);
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME));

    Mockito.verify(sender).sendMessage(String.format(TextConstants.Commands.Punishment.PUNISHMENT_404, JACOB_USERNAME));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender, NEVER).sendMessage(Mockito.anyString());
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
    Assert.assertNull(config.getMuteManager().getMuteData(JACOB_UUID));
  }

  @Test
  public void apiException() {
    // intentionally cause the call to fail by assigning a fake player uuid
    UUID invalidUuid = Mockito.mock(UUID.class);
    Mockito.when(invalidUuid.toString()).thenReturn("ayy lmao");
    offender = newStubbedPlayer(JACOB_USERNAME, invalidUuid, true);
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME));

    Mockito.verify(sender).sendMessage(ChatColor.DARK_RED + "Unexpected plugin exception");
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender, NEVER).sendMessage(Mockito.anyString());
    Mockito.verify(config.getLogging(), ONCE).logStackTrace(Mockito.any());
    Assert.assertNull(config.getMuteManager().getMuteData(JACOB_UUID));
  }

  @Test
  public void successMessage_temp_withReason() {
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME, "cheating", "--duration=24h"));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully muted JacobCrofts"));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());

    expectedMuteReason = "cheating";
    expectedMuteExpirationTime = System.currentTimeMillis() + 1000 * 60 * 60 * 24;
    verifyMuteData();
  }

  @Test
  public void successMessage_local_temp_withReason() {
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME, "cheating", "--local", "--duration=24h"));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully muted JacobCrofts"));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());

    expectedMuteReason = "cheating";
    expectedMuteExpirationTime = System.currentTimeMillis() + 1000 * 60 * 60 * 24;
    verifyMuteData();
  }

  @Test
  public void successMessage_temp_noReason() {
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME, "--duration=24h"));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully muted JacobCrofts"));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());

    expectedMuteExpirationTime = System.currentTimeMillis() + 1000 * 60 * 60 * 24;
    verifyMuteData();
  }

  @Test
  public void successMessage_local_temp_noReason() {
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME, "--local", "--duration=24h"));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully muted JacobCrofts"));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());

    expectedMuteExpirationTime = System.currentTimeMillis() + 1000 * 60 * 60 * 24;
    verifyMuteData();
  }

  @Test
  public void visiblePunishmentBroadcastMessage_temp() {
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME, "cheating", "--broadcast", "--duration=24h"));
    Mockito.verify(config.getBukkitWrapper(), ONCE).broadcastMessage(ChatColor.GRAY + "JacobCrofts has been muted");

    expectedMuteReason = "cheating";
    expectedMuteExpirationTime = System.currentTimeMillis() + 1000 * 60 * 60 * 24;
    verifyMuteData();
  }

  @Test
  public void visiblePunishmentBroadcastMessage() {
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME, "cheating", "--broadcast"));
    Mockito.verify(config.getBukkitWrapper(), ONCE).broadcastMessage(ChatColor.GRAY + "JacobCrofts has been muted");

    expectedMuteReason = "cheating";
    verifyMuteData();
  }

  @Test
  public void bothVisibleAndShadowFlags() {
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME, "cheating", "--broadcast", "--shadow"));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully muted JacobCrofts"));
    Mockito.verify(sender).sendMessage(TextConstants.Commands.BROADCAST_FLAG_IGNORED_MESSAGE);
    Mockito.verify(sender, TWICE).sendMessage(Mockito.anyString());
    Mockito.verify(config.getBukkitWrapper(), NEVER).broadcastMessage(Mockito.any());

    expectedMuteReason = "cheating";
    expectedShadow = true;
    verifyMuteData();
  }

  @Test
  public void bothVisibleAndPrivateFlags() {
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME, "cheating", "--broadcast", "--private"));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully muted JacobCrofts"));
    Mockito.verify(sender).sendMessage(TextConstants.Commands.BROADCAST_FLAG_IGNORED_MESSAGE);
    Mockito.verify(sender, TWICE).sendMessage(Mockito.anyString());
    Mockito.verify(config.getBukkitWrapper(), NEVER).broadcastMessage(Mockito.any());

    expectedMuteReason = "cheating";
    verifyMuteData();
  }

  @Test
  public void bothShadowAndPrivateFlags() {
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME, "cheating", "--shadow", "--private"));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully muted JacobCrofts"));
    Mockito.verify(sender).sendMessage(TextConstants.Commands.PRIVATE_FLAG_REDUNDANT_MESSAGE);
    Mockito.verify(sender, TWICE).sendMessage(Mockito.anyString());
    Mockito.verify(config.getBukkitWrapper(), NEVER).broadcastMessage(Mockito.any());

    expectedMuteReason = "cheating";
    expectedShadow = true;
    verifyMuteData();
  }

  @Test
  public void visibleShadowAndPrivateFlags() {
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME, "cheating", "--broadcast", "--private", "--shadow"));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully muted JacobCrofts"));
    Mockito.verify(sender).sendMessage(TextConstants.Commands.BROADCAST_FLAG_IGNORED_MESSAGE);
    Mockito.verify(sender).sendMessage(TextConstants.Commands.PRIVATE_FLAG_REDUNDANT_MESSAGE);
    Mockito.verify(sender, Mockito.times(3)).sendMessage(Mockito.anyString());
    Mockito.verify(config.getBukkitWrapper(), NEVER).broadcastMessage(Mockito.any());

    expectedMuteReason = "cheating";
    expectedShadow = true;
    verifyMuteData();
  }

  @Test
  public void successMessage_local_perm_withReason() {
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME, "cheating", "--local"));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully muted JacobCrofts"));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());

    expectedMuteReason = "cheating";
    verifyMuteData();
  }

  @Test
  public void shadow() {
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME, "cheating", "--shadow"));
    Mockito.verify(offender, NEVER).sendMessage(Mockito.anyString());

    expectedMuteReason = "cheating";
    expectedShadow = true;
    verifyMuteData();
  }


  private void stubOfflineOffender(ResolvedPlayer.Status httpStatus) {
    stubResolvedPlayer(offender.getName(), offender.getUniqueId(), httpStatus);
    Mockito.when(config.getBukkitWrapper().getOnlinePlayer(offender.getName())).thenReturn(null);
  }

  private void verifyMuteData() {
    verifyMuteData(false);
  }

  private void verifyMuteData(boolean playerSender) {
    MuteData muteData = config.getMuteManager().getMuteData(JACOB_UUID);
    Assert.assertNotNull(muteData);
    Assert.assertEquals(JACOB_UUID, muteData.getUuid());
    Assert.assertEquals(expectedMuteReason, muteData.getReason());
    if (expectedMuteExpirationTime == null) {
      Assert.assertNull(muteData.getExpiresAt());
    } else {
      Assert.assertNotNull(muteData.getExpiresAt());
      // cannot be off by more than 10 seconds
      Assert.assertTrue(Math.abs(muteData.getExpiresAt() - expectedMuteExpirationTime) < 1000 * 10);
    }
    Assert.assertEquals(expectedShadow, muteData.isShadow());
    if (playerSender) {
      Assert.assertEquals(JACOB_UUID, muteData.getIssuerId());
    } else {
      Assert.assertNull(muteData.getIssuerId());
    }
    Assert.assertNotNull(muteData.getId());
    Assert.assertTrue(muteData.getId().startsWith("MUTE-"));
  }

  private void onCommand(String... args) {
    String[] argsIncludingBaseCommand = new String[2 + args.length];
    argsIncludingBaseCommand[0] = "mutes";
    argsIncludingBaseCommand[1] = "create";
    System.arraycopy(args, 0, argsIncludingBaseCommand, 2, argsIncludingBaseCommand.length - 2);
    new CmdMMC(config).onCommand(sender, argsIncludingBaseCommand);
  }
}
