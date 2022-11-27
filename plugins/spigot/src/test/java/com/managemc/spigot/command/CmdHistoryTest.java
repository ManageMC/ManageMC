package com.managemc.spigot.command;

import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.spigot.command.CmdHistory;
import com.managemc.spigot.command.util.CommandAssertions;
import com.managemc.spigot.command.util.punishments.model.ResolvedPlayer;
import com.managemc.spigot.testutil.TestBase;
import com.managemc.spigot.testutil.TestConstants;
import com.managemc.spigot.util.TextConstants;
import com.managemc.spigot.util.permissions.Permission;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.UUID;

public class CmdHistoryTest extends TestBase {

  private static final String OFFENDER_USERNAME = "codename_B";
  private static final UUID OFFENDER_UUID = UUID.fromString("dba2f115-6cc2-401b-a9e9-b4d27f7c897d");

  @Before
  public void setup() {
    Mockito.when(sender.hasPermission(Mockito.anyString())).thenReturn(false);
    Mockito.when(sender.hasPermission(Permission.PUNISHMENTS_VIEW.getName())).thenReturn(true);
  }

  @Test
  public void noArgs() {
    onCommand(false);
    expectAbort(CommandAssertions.WRONG_NUM_ARGS);
  }

  @Test
  public void tooManyArgs() {
    onCommand(false, "ayy", "lmao");
    expectAbort(CommandAssertions.WRONG_NUM_ARGS);
  }

  @Test
  public void invalidUsername() {
    onCommand(false, "@@@");
    expectAbort("Invalid username @@@");
  }

  @Test
  public void onlineRecently() {
    stubResolvedPlayer(TestConstants.JACOB_USERNAME, TestConstants.JACOB_UUID, ResolvedPlayer.Status.ONLINE_RECENTLY);
    awaitAsyncCommand(() -> onCommand(true, TestConstants.JACOB_USERNAME));

    expectSingleMessage(String.format(CmdHistory.NO_PUNISHMENTS_MESSAGE, JACOB_USERNAME));
  }

  @Test
  public void offlineButReal() {
    stubResolvedPlayer(TestConstants.JACOB_USERNAME, TestConstants.JACOB_UUID, ResolvedPlayer.Status.HTTP_OK);
    awaitAsyncCommand(() -> onCommand(true, TestConstants.JACOB_USERNAME));

    expectSingleMessage(String.format(CmdHistory.NO_PUNISHMENTS_MESSAGE, JACOB_USERNAME));
  }

  @Test
  public void offlineNotFound() {
    stubResolvedPlayer(TestConstants.JACOB_USERNAME, TestConstants.JACOB_UUID, ResolvedPlayer.Status.HTTP_NOT_FOUND);
    awaitAsyncCommand(() -> onCommand(true, TestConstants.JACOB_USERNAME));

    expectAbort(CommandAssertions.NOT_FOUND_MSG);
  }

  @Test
  public void offlineRateLimitExceeded() {
    stubResolvedPlayer(TestConstants.JACOB_USERNAME, TestConstants.JACOB_UUID, ResolvedPlayer.Status.HTTP_RATE_LIMIT_EXCEEDED);
    awaitAsyncCommand(() -> onCommand(true, TestConstants.JACOB_USERNAME));

    expectAbort(CommandAssertions.MOJANG_RATE_LIMIT_MSG);
  }

  @Test
  public void offlineHttpUnexpected() {
    stubResolvedPlayer(TestConstants.JACOB_USERNAME, TestConstants.JACOB_UUID, ResolvedPlayer.Status.HTTP_UNEXPECTED);
    awaitAsyncCommand(() -> onCommand(true, TestConstants.JACOB_USERNAME));

    Mockito.verify(sender).sendMessage(ChatColor.DARK_RED + "Unexpected plugin exception");
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(config.getLogging(), ONCE).logStackTrace(Mockito.any());
  }

  @Test
  public void offlineUuidResolverClientException() {
    stubResolvedPlayer(TestConstants.JACOB_USERNAME, TestConstants.JACOB_UUID, ResolvedPlayer.Status.CLIENT_EXCEPTION);
    awaitAsyncCommand(() -> onCommand(true, TestConstants.JACOB_USERNAME));

    Mockito.verify(sender).sendMessage(ChatColor.DARK_RED + "Unexpected plugin exception");
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(config.getLogging(), ONCE).logStackTrace(Mockito.any());
  }

  @Test
  public void apiNotFound() {
    stubOffender(UUID.randomUUID());
    awaitAsyncCommand(() -> onCommand(true, OFFENDER_USERNAME));

    expectSingleMessage(String.format(TextConstants.Commands.Punishment.PUNISHMENT_404, OFFENDER_USERNAME));
  }

  @Test
  public void apiException() {
    // intentionally cause the call to fail by assigning a syntactically invalid UUID
    UUID invalidUuid = Mockito.mock(UUID.class);
    Mockito.when(invalidUuid.toString()).thenReturn("ayy lmao");
    stubOffender(invalidUuid);
    awaitAsyncCommand(() -> onCommand(true, OFFENDER_USERNAME));

    Mockito.verify(sender).sendMessage(ChatColor.DARK_RED + "Unexpected plugin exception");
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(config.getLogging(), ONCE).logStackTrace(Mockito.any());
  }

  @Test
  public void withPunishments() {
    // this one will be added again by API call, and we want to make sure there are no duplicates
    config.getPardonablePunishmentData().addPunishment(sender, "ban-8");

    // these aren't real but should not be removed
    config.getPardonablePunishmentData().addPunishment(sender, "ban-0");
    config.getPardonablePunishmentData().addPunishment(sender, "ban-01");
    Assert.assertEquals(3, config.getPardonablePunishmentData().getPunishmentIdsKnownToSender(sender).size());

    stubOffender(OFFENDER_UUID);
    awaitAsyncCommand(() -> onCommand(true, OFFENDER_USERNAME));
    Mockito.verify(sender).sendMessage(Mockito.contains("Punishment History for " + OFFENDER_USERNAME));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());

    // 6 unique punishments plus two fake ones we added earlier
    Assert.assertEquals(8, config.getPardonablePunishmentData().getPunishmentIdsKnownToSender(sender).size());
  }

  @Test
  public void noPermission_clientSide() {
    stubOffender(OFFENDER_UUID);
    sender = Mockito.mock(Player.class);
    Mockito.when(sender.hasPermission(Mockito.anyString())).thenReturn(false);
    onCommand(true, OFFENDER_USERNAME);

    expectAbort(CommandAssertions.NO_PERMS_MESSAGE);
  }

  @Test
  public void noPermission_serverSide() {
    stubOffender(OFFENDER_UUID);
    sender = Mockito.mock(Player.class);
    Mockito.when(sender.hasPermission(Mockito.anyString())).thenReturn(true);
    Mockito.when(((Player) sender).getUniqueId()).thenReturn(TestConstants.PHYLLIS_UUID);
    awaitAsyncCommand(() -> onCommand(true, OFFENDER_USERNAME));

    expectSingleMessage(TextConstants.INSUFFICIENT_PERMS);
  }


  private void stubOffender(UUID uuid) {
    stubResolvedPlayer(OFFENDER_USERNAME, uuid, ResolvedPlayer.Status.ONLINE_RECENTLY);
  }

  private void onCommand(boolean noSyntaxError, String... args) {
    CommandExecutorAsync cmd = new CmdHistory(config);
    Assert.assertEquals(noSyntaxError, cmd.onCommand(sender, command, "", args));
  }
}
