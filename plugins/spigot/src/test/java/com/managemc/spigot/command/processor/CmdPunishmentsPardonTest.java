package com.managemc.spigot.command.processor;

import com.managemc.api.wrapper.ClientProvider;
import com.managemc.spigot.command.handler.CmdPunishmentsPardonHandler;
import com.managemc.spigot.command.util.CommandAssertions;
import com.managemc.spigot.command.util.CommandNodeMap;
import com.managemc.spigot.command.util.CommandUsage;
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

public class CmdPunishmentsPardonTest extends TestBase {

  @Test
  public void noArgs() {
    onCommand();
    assertSyntactic(CommandAssertions.WRONG_NUM_ARGS, CommandUsage.PUNISHMENTS_PARDON);
  }

  @Test
  public void invalidPunishmentId1() {
    onCommand("OOPS-1");
    assertSyntactic(CmdPunishmentsPardonHandler.BAD_ID_MESSAGE, CommandUsage.PUNISHMENTS_PARDON);
  }

  @Test
  public void invalidPunishmentId2() {
    onCommand("BAN-");
    assertSyntactic(CmdPunishmentsPardonHandler.BAD_ID_MESSAGE, CommandUsage.PUNISHMENTS_PARDON);
  }

  @Test
  public void punishmentNotFound() {
    config.getPardonablePunishmentData().addPunishment(sender, "BAN-0");
    Assert.assertEquals(1, config.getPardonablePunishmentData().getPunishmentIdsKnownToSender(sender).size());

    awaitAsyncCommand(() -> onCommand("BAN-0"));
    Mockito.verify(sender).sendMessage(PardonService.NOT_FOUND_MESSAGE);
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());

    Assert.assertEquals(1, config.getPardonablePunishmentData().getPunishmentIdsKnownToSender(sender).size());
  }

  @Test
  public void inactivePunishment() {
    awaitAsyncCommand(() -> onCommand("BAN-10"));
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

    awaitAsyncCommand(() -> onCommand("BAN-9"));
    Mockito.verify(sender).sendMessage(String.format(PardonService.SUCCESS_MESSAGE, 1));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());

    Assert.assertEquals(1, punishmentData.getPunishmentIdsKnownToSender(sender).size());
    Assert.assertEquals("ban-10", punishmentData.getPunishmentIdsKnownToSender(sender).get(0));
    Assert.assertEquals(1, punishmentData.getPunishmentIdsKnownToSender(randomUuid).size());
    Assert.assertEquals("ban-10", punishmentData.getPunishmentIdsKnownToSender(randomUuid).get(0));
  }

  @Test
  public void happyPath_lowerCaseTypeMultipleIdsWithDetails() {
    awaitAsyncCommand(() -> onCommand("ban-9", "ban-36", "why", "not"));
    Mockito.verify(sender).sendMessage(String.format(PardonService.SUCCESS_MESSAGE, 2));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
  }

  @Test
  public void happyPath_offenderWithNoUsername() {
    awaitAsyncCommand(() -> onCommand("ban-36"));
    Mockito.verify(sender).sendMessage(String.format(PardonService.SUCCESS_MESSAGE, 1));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
  }

  @Test
  public void unexpectedResponseStatus() {
    ClientProvider webClients = Mockito.mock(ClientProvider.class);
    Mockito.when(webClients.externalServer()).thenThrow(new RuntimeException());
    config.setClientProvider(webClients);

    awaitAsyncCommand(() -> onCommand("BAN-1"));
    Mockito.verify(sender).sendMessage(ChatColor.DARK_RED + "Unexpected plugin exception");
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
  }

  @Test
  public void playerSender_ownerPermission() {
    stubPlayerSenderWithPermissions(TestConstants.JACOB_UUID, Permission.OWNER);

    awaitAsyncCommand(() -> onCommand("ban-36"));
    Mockito.verify(sender).sendMessage(String.format(PardonService.SUCCESS_MESSAGE, 1));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
  }

  @Test
  public void playerSender_adminPermission() {
    stubPlayerSenderWithPermissions(TestConstants.JACOB_UUID, Permission.ADMIN);

    awaitAsyncCommand(() -> onCommand("ban-36"));
    Mockito.verify(sender).sendMessage(String.format(PardonService.SUCCESS_MESSAGE, 1));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
  }

  @Test
  public void playerSender_banInGamePermission() {
    stubPlayerSenderWithPermissions(TestConstants.JACOB_UUID, Permission.BAN_IN_GAME);

    awaitAsyncCommand(() -> onCommand("ban-36"));
    Mockito.verify(sender).sendMessage(String.format(PardonService.SUCCESS_MESSAGE, 1));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
  }

  @Test
  public void playerSender_banWebPermission() {
    stubPlayerSenderWithPermissions(TestConstants.JACOB_UUID, Permission.BAN_WEB);

    awaitAsyncCommand(() -> onCommand("ban-36"));
    Mockito.verify(sender).sendMessage(String.format(PardonService.SUCCESS_MESSAGE, 1));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
  }

  @Test
  public void playerSender_banInsufficientPerms() {
    stubPlayerSenderWithPermissions(TestConstants.JACOB_UUID);

    onCommand("ban-36");
    Mockito.verify(sender).sendMessage(ChatColor.RED + CommandNodeMap.NO_PERMS_MSG);
  }

  @Test
  public void playerSender_muteInGamePermission() {
    stubPlayerSenderWithPermissions(TestConstants.JACOB_UUID, Permission.MUTE_IN_GAME);

    awaitAsyncCommand(() -> onCommand("mute-10"));
    Mockito.verify(sender).sendMessage(String.format(PardonService.SUCCESS_MESSAGE, 1));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
  }

  @Test
  public void playerSender_muteWebPermission() {
    stubPlayerSenderWithPermissions(TestConstants.JACOB_UUID, Permission.MUTE_WEB);

    awaitAsyncCommand(() -> onCommand("mute-10"));
    Mockito.verify(sender).sendMessage(String.format(PardonService.SUCCESS_MESSAGE, 1));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
  }

  @Test
  public void playerSender_muteInsufficientPerms() {
    stubPlayerSenderWithPermissions(TestConstants.JACOB_UUID);

    onCommand("mute-10");
    Mockito.verify(sender).sendMessage(ChatColor.RED + CommandNodeMap.NO_PERMS_MSG);
  }

  @Test
  public void playerSender_warningInGamePermission() {
    stubPlayerSenderWithPermissions(TestConstants.JACOB_UUID, Permission.WARN_IN_GAME);

    awaitAsyncCommand(() -> onCommand("warning-5"));
    Mockito.verify(sender).sendMessage(String.format(PardonService.SUCCESS_MESSAGE, 1));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
  }

  @Test
  public void playerSender_warningWebPermission() {
    stubPlayerSenderWithPermissions(TestConstants.JACOB_UUID, Permission.WARN_WEB);

    awaitAsyncCommand(() -> onCommand("warning-5"));
    Mockito.verify(sender).sendMessage(String.format(PardonService.SUCCESS_MESSAGE, 1));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
  }

  @Test
  public void playerSender_warningInsufficientPerms() {
    stubPlayerSenderWithPermissions(TestConstants.JACOB_UUID);

    onCommand("warning-5");
    Mockito.verify(sender).sendMessage(ChatColor.RED + CommandNodeMap.NO_PERMS_MSG);
  }

  @Test
  public void playerSender_ipBanInGamePermission() {
    stubPlayerSenderWithPermissions(TestConstants.JACOB_UUID, Permission.IP_BAN_IN_GAME);

    awaitAsyncCommand(() -> onCommand("ip_ban-1"));
    Mockito.verify(sender).sendMessage(String.format(PardonService.SUCCESS_MESSAGE, 1));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
  }

  @Test
  public void playerSender_ipBanWebPermission() {
    stubPlayerSenderWithPermissions(TestConstants.JACOB_UUID, Permission.IP_BAN_WEB);

    awaitAsyncCommand(() -> onCommand("ip_ban-1"));
    Mockito.verify(sender).sendMessage(String.format(PardonService.SUCCESS_MESSAGE, 1));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
  }

  @Test
  public void playerSender_ipBanInsufficientPerms() {
    stubPlayerSenderWithPermissions(TestConstants.JACOB_UUID);

    onCommand("ip_ban-1");
    Mockito.verify(sender).sendMessage(ChatColor.RED + CommandNodeMap.NO_PERMS_MSG);
  }

  // ID 28 just happens to be an IP range ban.
  @Test
  public void playerSender_ipRangeBanInGamePermission() {
    stubPlayerSenderWithPermissions(TestConstants.JACOB_UUID, Permission.IP_BAN_IN_GAME);

    awaitAsyncCommand(() -> onCommand("ip_ban-28"));
    Mockito.verify(sender).sendMessage(String.format(PardonService.SUCCESS_MESSAGE, 1));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
  }

  @Test
  public void playerSender_ipRangeBanWebPermission() {
    stubPlayerSenderWithPermissions(TestConstants.JACOB_UUID, Permission.IP_BAN_WEB);

    awaitAsyncCommand(() -> onCommand("ip_ban-28"));
    Mockito.verify(sender).sendMessage(String.format(PardonService.SUCCESS_MESSAGE, 1));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
  }

  @Test
  public void playerSender_ipRangeBanInsufficientPerms() {
    stubPlayerSenderWithPermissions(TestConstants.JACOB_UUID);

    onCommand("ip_ban-28");
    Mockito.verify(sender).sendMessage(ChatColor.RED + CommandNodeMap.NO_PERMS_MSG);
  }

  @Test
  public void playerSender_noPermsServerSide() {
    sender = Mockito.mock(Player.class);
    Mockito.when(((Player) sender).getUniqueId()).thenReturn(TestConstants.PHYLLIS_UUID);
    Mockito.when(sender.hasPermission(Mockito.anyString())).thenReturn(true);
    awaitAsyncCommand(() -> onCommand("ip_ban-28"));

    Mockito.verify(sender).sendMessage(TextConstants.INSUFFICIENT_PERMS);
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

    awaitAsyncCommand(() -> onCommand("mute-10", "mute-11"));

    Assert.assertNull(config.getMuteManager().getMuteData(onlinePlayer.getUniqueId()));
    Assert.assertNull(config.getMuteManager().getMuteData(offlinePlayer.getUniqueId()));
    Assert.assertNotNull(config.getMuteManager().getMuteData(irrelevantPlayer.getUniqueId()));

    Mockito.verify(onlinePlayer).sendMessage(PardonService.NO_LONGER_MUTED_MESSAGE);
    Mockito.verify(offlinePlayer, NEVER).sendMessage(Mockito.anyString());
    Mockito.verify(irrelevantPlayer, NEVER).sendMessage(Mockito.anyString());
  }


  private void onCommand(String... args) {
    String[] argsIncludingBaseCommand = new String[2 + args.length];
    argsIncludingBaseCommand[0] = "punishments";
    argsIncludingBaseCommand[1] = "pardon";
    System.arraycopy(args, 0, argsIncludingBaseCommand, 2, argsIncludingBaseCommand.length - 2);
    new CmdMMC(config).onCommand(sender, argsIncludingBaseCommand);
  }
}
