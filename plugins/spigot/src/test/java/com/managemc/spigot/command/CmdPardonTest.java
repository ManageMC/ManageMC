package com.managemc.spigot.command;

import com.managemc.api.wrapper.ClientProvider;
import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.spigot.command.CmdPardon;
import com.managemc.spigot.command.util.CommandAssertions;
import com.managemc.spigot.service.PardonService;
import com.managemc.spigot.state.PardonablePunishmentData;
import com.managemc.spigot.state.model.MuteData;
import com.managemc.spigot.testutil.TestBase;
import com.managemc.spigot.testutil.TestConstants;
import com.managemc.spigot.util.TextConstants;
import com.managemc.spigot.util.permissions.Permission;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.openapitools.client.model.BanOrMute;

import java.util.UUID;

public class CmdPardonTest extends TestBase {

  @Test
  public void noArgs() {
    onCommand(false);
    expectAbort(CommandAssertions.WRONG_NUM_ARGS);
  }

  @Test
  public void invalidPunishmentId1() {
    onCommand(false, "OOPS-1");
    expectAbort(CmdPardon.BAD_ID_MESSAGE);
  }

  @Test
  public void invalidPunishmentId2() {
    onCommand(false, "BAN-");
    expectAbort(CmdPardon.BAD_ID_MESSAGE);
  }

  @Test
  public void punishmentNotFound() {
    config.getPardonablePunishmentData().addPunishment(sender, "BAN-0");
    Assert.assertEquals(1, config.getPardonablePunishmentData().getPunishmentIdsKnownToSender(sender).size());

    awaitAsyncCommand(() -> onCommand(true, "BAN-0"));
    expectSingleMessage(PardonService.NOT_FOUND_MESSAGE);

    Assert.assertEquals(1, config.getPardonablePunishmentData().getPunishmentIdsKnownToSender(sender).size());
  }

  @Test
  public void inactivePunishment() {
    awaitAsyncCommand(() -> onCommand(true, "BAN-10"));
    Mockito.verify(sender).sendMessage(String.format(PardonService.SUCCESS_MESSAGE, 1));
    Mockito.verify(sender).sendMessage(PardonService.INACTIVE_PUNISHMENTS_WARNING);
    Mockito.verify(sender, Mockito.times(2)).sendMessage(Mockito.anyString());
  }

  @Test
  public void happyPath() {
    PardonablePunishmentData punishmentData = config.getPardonablePunishmentData();
    UUID randomUuid = UUID.randomUUID();
    punishmentData.addPunishment(sender, "BAN-9");
    punishmentData.addPunishment(sender, "BAN-10");
    punishmentData.addPunishment(randomUuid, "BAN-9");
    punishmentData.addPunishment(randomUuid, "BAN-10");
    Assert.assertEquals(2, punishmentData.getPunishmentIdsKnownToSender(sender).size());
    Assert.assertEquals(2, punishmentData.getPunishmentIdsKnownToSender(randomUuid).size());

    awaitAsyncCommand(() -> onCommand(true, "BAN-9"));
    expectSingleMessage(String.format(PardonService.SUCCESS_MESSAGE, 1));

    Assert.assertEquals(1, punishmentData.getPunishmentIdsKnownToSender(sender).size());
    Assert.assertEquals("ban-10", punishmentData.getPunishmentIdsKnownToSender(sender).get(0));
    Assert.assertEquals(1, punishmentData.getPunishmentIdsKnownToSender(randomUuid).size());
    Assert.assertEquals("ban-10", punishmentData.getPunishmentIdsKnownToSender(randomUuid).get(0));
  }

  @Test
  public void happyPath_lowerCaseTypeMultipleIdsWithDetails() {
    awaitAsyncCommand(() -> onCommand(true, "ban-9", "ban-36", "why", "not"));
    expectSingleMessage(String.format(PardonService.SUCCESS_MESSAGE, 2));
  }

  @Test
  public void happyPath_offenderWithNoUsername() {
    awaitAsyncCommand(() -> onCommand(true, "ban-36"));
    expectSingleMessage(String.format(PardonService.SUCCESS_MESSAGE, 1));
  }

  @Test
  public void unexpectedResponseStatus() {
    ClientProvider webClients = Mockito.mock(ClientProvider.class);
    Mockito.when(webClients.externalServer()).thenThrow(new RuntimeException());
    config.setClientProvider(webClients);

    awaitAsyncCommand(() -> onCommand(true, "BAN-1"));
    Mockito.verify(sender).sendMessage(ChatColor.DARK_RED + "Unexpected plugin exception");
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
  }

  @Test
  public void playerSender_ownerPermission() {
    stubPlayerSenderWithPermissions(TestConstants.JACOB_UUID, Permission.OWNER);

    awaitAsyncCommand(() -> onCommand(true, "ban-36"));
    expectSingleMessage(String.format(PardonService.SUCCESS_MESSAGE, 1));
  }

  @Test
  public void playerSender_adminPermission() {
    stubPlayerSenderWithPermissions(TestConstants.JACOB_UUID, Permission.ADMIN);

    awaitAsyncCommand(() -> onCommand(true, "ban-36"));
    expectSingleMessage(String.format(PardonService.SUCCESS_MESSAGE, 1));
  }

  @Test
  public void playerSender_banInGamePermission() {
    stubPlayerSenderWithPermissions(TestConstants.JACOB_UUID, Permission.BAN_IN_GAME);

    awaitAsyncCommand(() -> onCommand(true, "ban-36"));
    expectSingleMessage(String.format(PardonService.SUCCESS_MESSAGE, 1));
  }

  @Test
  public void playerSender_banWebPermission() {
    stubPlayerSenderWithPermissions(TestConstants.JACOB_UUID, Permission.BAN_WEB);

    awaitAsyncCommand(() -> onCommand(true, "ban-36"));
    expectSingleMessage(String.format(PardonService.SUCCESS_MESSAGE, 1));
  }

  @Test
  public void playerSender_banInsufficientPerms() {
    stubPlayerSenderWithPermissions(TestConstants.JACOB_UUID);

    onCommand(true, "ban-36");
    expectAbort(CommandAssertions.NO_PERMS_MESSAGE);
  }

  @Test
  public void playerSender_muteInGamePermission() {
    stubPlayerSenderWithPermissions(TestConstants.JACOB_UUID, Permission.MUTE_IN_GAME);

    awaitAsyncCommand(() -> onCommand(true, "mute-10"));
    expectSingleMessage(String.format(PardonService.SUCCESS_MESSAGE, 1));
  }

  @Test
  public void playerSender_muteWebPermission() {
    stubPlayerSenderWithPermissions(TestConstants.JACOB_UUID, Permission.MUTE_WEB);

    awaitAsyncCommand(() -> onCommand(true, "mute-10"));
    expectSingleMessage(String.format(PardonService.SUCCESS_MESSAGE, 1));
  }

  @Test
  public void playerSender_muteInsufficientPerms() {
    stubPlayerSenderWithPermissions(TestConstants.JACOB_UUID);

    onCommand(true, "mute-10");
    expectAbort(CommandAssertions.NO_PERMS_MESSAGE);
  }

  @Test
  public void playerSender_warningInGamePermission() {
    stubPlayerSenderWithPermissions(TestConstants.JACOB_UUID, Permission.WARN_IN_GAME);

    awaitAsyncCommand(() -> onCommand(true, "warning-5"));
    expectSingleMessage(String.format(PardonService.SUCCESS_MESSAGE, 1));
  }

  @Test
  public void playerSender_warningWebPermission() {
    stubPlayerSenderWithPermissions(TestConstants.JACOB_UUID, Permission.WARN_WEB);

    awaitAsyncCommand(() -> onCommand(true, "warning-5"));
    expectSingleMessage(String.format(PardonService.SUCCESS_MESSAGE, 1));
  }

  @Test
  public void playerSender_warningInsufficientPerms() {
    stubPlayerSenderWithPermissions(TestConstants.JACOB_UUID);

    onCommand(true, "warning-5");
    expectAbort(CommandAssertions.NO_PERMS_MESSAGE);
  }

  @Test
  public void playerSender_ipBanInGamePermission() {
    stubPlayerSenderWithPermissions(TestConstants.JACOB_UUID, Permission.IP_BAN_IN_GAME);

    awaitAsyncCommand(() -> onCommand(true, "ip_ban-1"));
    expectSingleMessage(String.format(PardonService.SUCCESS_MESSAGE, 1));
  }

  @Test
  public void playerSender_ipBanWebPermission() {
    stubPlayerSenderWithPermissions(TestConstants.JACOB_UUID, Permission.IP_BAN_WEB);

    awaitAsyncCommand(() -> onCommand(true, "ip_ban-1"));
    expectSingleMessage(String.format(PardonService.SUCCESS_MESSAGE, 1));
  }

  @Test
  public void playerSender_ipBanInsufficientPerms() {
    stubPlayerSenderWithPermissions(TestConstants.JACOB_UUID);

    onCommand(true, "ip_ban-1");
    expectAbort(CommandAssertions.NO_PERMS_MESSAGE);
  }

  // ID 28 just happens to be an IP range ban.
  @Test
  public void playerSender_ipRangeBanInGamePermission() {
    stubPlayerSenderWithPermissions(TestConstants.JACOB_UUID, Permission.IP_BAN_IN_GAME);

    awaitAsyncCommand(() -> onCommand(true, "ip_ban-28"));
    expectSingleMessage(String.format(PardonService.SUCCESS_MESSAGE, 1));
  }

  @Test
  public void playerSender_ipRangeBanWebPermission() {
    stubPlayerSenderWithPermissions(TestConstants.JACOB_UUID, Permission.IP_BAN_WEB);

    awaitAsyncCommand(() -> onCommand(true, "ip_ban-28"));
    expectSingleMessage(String.format(PardonService.SUCCESS_MESSAGE, 1));
  }

  @Test
  public void playerSender_ipRangeBanInsufficientPerms() {
    stubPlayerSenderWithPermissions(TestConstants.JACOB_UUID);

    onCommand(true, "ip_ban-28");
    expectAbort(CommandAssertions.NO_PERMS_MESSAGE);
  }

  @Test
  public void playerSender_noPermsServerSide() {
    sender = Mockito.mock(Player.class);
    Mockito.when(((Player) sender).getUniqueId()).thenReturn(TestConstants.PHYLLIS_UUID);
    Mockito.when(sender.hasPermission(Mockito.anyString())).thenReturn(true);
    awaitAsyncCommand(() -> onCommand(true, "ip_ban-28"));

    expectSingleMessage(TextConstants.INSUFFICIENT_PERMS);
  }

  @Test
  public void pardoningMutesShouldClearThemFromLocalState() {
    Player onlinePlayer = newStubbedPlayer("bob1", UUID.randomUUID(), true);
    Player offlinePlayer = newStubbedPlayer("bob2", UUID.randomUUID(), false);
    Player irrelevantPlayer = newStubbedPlayer("bob3", UUID.randomUUID(), true);

    config.getMuteManager().setMuted(new MuteData(onlinePlayer.getUniqueId(), new BanOrMute().externalId("MUTE-10")));
    config.getMuteManager().setMuted(new MuteData(offlinePlayer.getUniqueId(), new BanOrMute().externalId("MUTE-11")));
    config.getMuteManager().setMuted(new MuteData(irrelevantPlayer.getUniqueId(), new BanOrMute().externalId("MUTE-12")));

    Assert.assertNotNull(config.getMuteManager().getMuteData(onlinePlayer.getUniqueId()));
    Assert.assertNotNull(config.getMuteManager().getMuteData(offlinePlayer.getUniqueId()));
    Assert.assertNotNull(config.getMuteManager().getMuteData(irrelevantPlayer.getUniqueId()));

    awaitAsyncCommand(() -> onCommand(true, "mute-10", "mute-11"));

    Assert.assertNull(config.getMuteManager().getMuteData(onlinePlayer.getUniqueId()));
    Assert.assertNull(config.getMuteManager().getMuteData(offlinePlayer.getUniqueId()));
    Assert.assertNotNull(config.getMuteManager().getMuteData(irrelevantPlayer.getUniqueId()));

    Mockito.verify(onlinePlayer).sendMessage(PardonService.NO_LONGER_MUTED_MESSAGE);
    Mockito.verify(offlinePlayer, NEVER).sendMessage(Mockito.anyString());
    Mockito.verify(irrelevantPlayer, NEVER).sendMessage(Mockito.anyString());
  }


  private void onCommand(boolean noSyntaxError, String... args) {
    CommandExecutorAsync cmd = new CmdPardon(config);
    Assert.assertEquals(noSyntaxError, cmd.onCommand(sender, command, "", args));
  }
}
