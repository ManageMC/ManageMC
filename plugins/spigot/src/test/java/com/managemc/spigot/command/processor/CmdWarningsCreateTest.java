package com.managemc.spigot.command.processor;

import com.managemc.spigot.command.util.CommandAssertions;
import com.managemc.spigot.command.util.CommandNodeMap;
import com.managemc.spigot.command.util.CommandUsage;
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

public class CmdWarningsCreateTest extends TestBase {

  private Player offender;

  @Before
  public void setup() {
    offender = newStubbedPlayer(JACOB_USERNAME, JACOB_UUID, true);
  }

  @Test
  public void whenNoArguments_shouldAbortWithUsageMessage() {
    onCommand();
    assertSyntactic(CommandAssertions.WRONG_NUM_ARGS, CommandUsage.WARNINGS_CREATE);
  }

  @Test
  public void whenUsernameIsInvalid_shouldAbortWithUsageMessage() {
    onCommand("!@#^%*");
    assertSyntactic("Invalid username !@#^%*", CommandUsage.WARNINGS_CREATE);
  }

  @Test
  public void online_noReason() {
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME));

    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully warned JacobCrofts"));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.WARNING_MESSAGE));
    Mockito.verify(offender).kickPlayer(Mockito.contains("reason: " + ChatColor.RESET));
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());

    Assert.assertEquals(1, config.getPardonablePunishmentData().getPunishmentIdsKnownToSender(sender).size());
    String externalId = config.getPardonablePunishmentData().getPunishmentIdsKnownToSender(sender).get(0);
    Assert.assertEquals("warning", externalId.split("-")[0]);
  }

  @Test
  public void online_withReason_oneWord() {
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME, "shh"));

    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully warned JacobCrofts"));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.WARNING_MESSAGE));
    Mockito.verify(offender).kickPlayer(Mockito.contains("reason: shh" + ChatColor.RESET));
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
  }

  @Test
  public void online_withReason_multipleWords() {
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME, "\"being", "annoying\"", "and", "some", "details"));

    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully warned JacobCrofts"));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.WARNING_MESSAGE));
    Mockito.verify(offender).kickPlayer(Mockito.contains("reason: being annoying" + ChatColor.RESET));
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
  }

  @Test
  public void offline_onlineRecently() {
    stubOfflineOffender(ResolvedPlayer.Status.ONLINE_RECENTLY);
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME));

    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully warned JacobCrofts"));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender, NEVER).kickPlayer(Mockito.any());
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
  }

  @Test
  public void offline() {
    stubOfflineOffender(ResolvedPlayer.Status.HTTP_OK);
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME));

    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully warned JacobCrofts"));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender, NEVER).kickPlayer(Mockito.any());
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
  }

  @Test
  public void offline_notFound() {
    stubOfflineOffender(ResolvedPlayer.Status.HTTP_NOT_FOUND);
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME));

    Mockito.verify(sender).sendMessage(ChatColor.RED + CommandAssertions.NOT_FOUND_MSG);
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender, NEVER).kickPlayer(Mockito.any());
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
  }

  @Test
  public void offline_rateLimitExceeded() {
    stubOfflineOffender(ResolvedPlayer.Status.HTTP_RATE_LIMIT_EXCEEDED);
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME));

    Mockito.verify(sender).sendMessage(ChatColor.RED + CommandAssertions.MOJANG_RATE_LIMIT_MSG);
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender, NEVER).kickPlayer(Mockito.any());
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
  }

  @Test
  public void offline_unexpectedHttpStatus() {
    stubOfflineOffender(ResolvedPlayer.Status.HTTP_UNEXPECTED);
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME));

    Mockito.verify(sender).sendMessage(ChatColor.DARK_RED + "Unexpected plugin exception");
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender, NEVER).kickPlayer(Mockito.any());
    Mockito.verify(config.getLogging(), ONCE).logStackTrace(Mockito.any());
  }

  @Test
  public void offline_uuidResolverClientError() {
    stubOfflineOffender(ResolvedPlayer.Status.CLIENT_EXCEPTION);
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME));

    Mockito.verify(sender).sendMessage(ChatColor.DARK_RED + "Unexpected plugin exception");
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender, NEVER).kickPlayer(Mockito.any());
    Mockito.verify(config.getLogging(), ONCE).logStackTrace(Mockito.any());
  }

  @Test
  public void playerSender_ownerPermission() {
    stubPlayerSenderWithPermissions(TestConstants.JACOB_UUID, Permission.OWNER);
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME));

    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully warned JacobCrofts"));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.WARNING_MESSAGE));
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());

    Assert.assertEquals(1, config.getPardonablePunishmentData().getPunishmentIdsKnownToSender(sender).size());
    String externalId = config.getPardonablePunishmentData().getPunishmentIdsKnownToSender(sender).get(0);
    Assert.assertEquals("warning", externalId.split("-")[0]);
  }

  @Test
  public void playerSender_adminPermission() {
    stubPlayerSenderWithPermissions(TestConstants.JACOB_UUID, Permission.ADMIN);
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME));

    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully warned JacobCrofts"));
  }

  @Test
  public void playerSender_warnInGamePermission() {
    stubPlayerSenderWithPermissions(TestConstants.JACOB_UUID, Permission.WARN_IN_GAME);
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME));

    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully warned JacobCrofts"));
  }

  @Test
  public void playerSender_warnWebPermission() {
    stubPlayerSenderWithPermissions(TestConstants.JACOB_UUID, Permission.WARN_WEB);
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME));

    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully warned JacobCrofts"));
  }

  @Test
  public void playerSender_noPerms() {
    stubPlayerSenderWithPermissions(TestConstants.JACOB_UUID);
    onCommand(JACOB_USERNAME);

    Mockito.verify(sender).sendMessage(ChatColor.RED + CommandNodeMap.NO_PERMS_MSG);
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
  }

  @Test
  public void playerSender_noPermsServerSide() {
    sender = Mockito.mock(Player.class);
    Mockito.when(((Player) sender).getUniqueId()).thenReturn(TestConstants.PHYLLIS_UUID);
    Mockito.when(sender.hasPermission(Mockito.anyString())).thenReturn(true);
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME));

    Mockito.verify(sender).sendMessage(TextConstants.INSUFFICIENT_PERMS);
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
  }

  @Test
  public void playerNotFound() {
    offender = newStubbedPlayer(JACOB_USERNAME, UUID.randomUUID(), true);
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME, "spamming", "and", "stuff"));

    Mockito.verify(sender).sendMessage(String.format(TextConstants.Commands.Punishment.PUNISHMENT_404, JACOB_USERNAME));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.WARNING_MESSAGE));
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
  }

  @Test
  public void apiException() {
    // intentionally cause the call to fail by assigning a syntactically invalid UUID
    UUID invalidUuid = Mockito.mock(UUID.class);
    Mockito.when(invalidUuid.toString()).thenReturn("ayy lmao");
    offender = newStubbedPlayer(JACOB_USERNAME, invalidUuid, true);
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME, "spamming", "and", "stuff"));

    Mockito.verify(sender).sendMessage(ChatColor.DARK_RED + "Unexpected plugin exception");
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.WARNING_MESSAGE));
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
    Mockito.verify(config.getLogging(), ONCE).logStackTrace(Mockito.any());
  }


  private void stubOfflineOffender(ResolvedPlayer.Status httpStatus) {
    stubResolvedPlayer(offender.getName(), offender.getUniqueId(), httpStatus);
    Mockito.when(config.getBukkitWrapper().getOnlinePlayer(offender.getName())).thenReturn(null);
  }

  private void onCommand(String... args) {
    String[] argsIncludingBaseCommand = new String[2 + args.length];
    argsIncludingBaseCommand[0] = "warnings";
    argsIncludingBaseCommand[1] = "create";
    System.arraycopy(args, 0, argsIncludingBaseCommand, 2, argsIncludingBaseCommand.length - 2);
    new CmdMMC(config).onCommand(sender, argsIncludingBaseCommand);
  }
}
