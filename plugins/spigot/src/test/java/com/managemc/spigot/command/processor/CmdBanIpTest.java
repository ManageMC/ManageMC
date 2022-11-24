package com.managemc.spigot.command.processor;

import com.managemc.api.ApiException;
import com.managemc.api.api.PunishmentsApi;
import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.spigot.command.util.CommandAssertions;
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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.UUID;

public class CmdBanIpTest extends TestBase {

  private static final String IP_ADDRESS = "12.13.14.15";
  private static final String IP_ADDRESS_RANGE_COVERING_PLAYER = "12.0.0.0-13.0.0.0";

  private Player offender;
  private Player phyllis;
  private Player differentIpPlayer;
  private Player nullIpPlayer;

  @Before
  public void setup() {
    offender = newStubbedPlayer(JACOB_USERNAME, JACOB_UUID, true);
    mockIpAddress(offender, IP_ADDRESS);
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
  public void syntacticallyInvalidIpAddress() {
    onCommand(false, "256.0.0.0");
    expectAbort("Invalid IPv4 address 256.0.0.0");
  }

  @Test
  public void syntacticallyInvalidIpAddressRange() {
    onCommand(false, "255.0.0.0-256.0.0.0");
    expectAbort("Invalid IP address range 255.0.0.0-256.0.0.0");
  }

  @Test
  public void offenderOffline() {
    Mockito.when(config.getBukkitWrapper().getOnlineOrRecentlyOnlinePlayer(JACOB_USERNAME)).thenReturn(null);
    onCommand(true, JACOB_USERNAME);
    expectAbort(CmdBanIp.OFFLINE_MSG);
  }

  @Test
  public void offenderOfflineButCached() {
    Mockito.when(offender.isOnline()).thenReturn(false);
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully IP-banned " + IP_ADDRESS));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender, NEVER).kickPlayer(Mockito.anyString());

    Assert.assertEquals(1, config.getPardonablePunishmentData().getPunishmentIdsKnownToSender(sender).size());
    String externalId = config.getPardonablePunishmentData().getPunishmentIdsKnownToSender(sender).get(0);
    Assert.assertEquals("ip_ban", externalId.split("-")[0]);
  }

  @Test
  public void offenderIpAddressIsNull() {
    Mockito.when(offender.getAddress()).thenReturn(null);
    onCommand(true, JACOB_USERNAME);
    expectAbort(CmdBanIp.NO_IP_MSG);
  }

  @Test
  public void playerSender_ownerPermission() {
    stubPlayerSenderWithPermissions(JACOB_UUID, Permission.OWNER);
    awaitAsyncCommand(() -> onCommand(true, "1.3.4.5"));

    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully IP-banned 1.3.4.5"));
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());

    Assert.assertEquals(1, config.getPardonablePunishmentData().getPunishmentIdsKnownToSender(sender).size());
    String externalId = config.getPardonablePunishmentData().getPunishmentIdsKnownToSender(sender).get(0);
    Assert.assertEquals("ip_ban", externalId.split("-")[0]);
  }

  @Test
  public void playerSender_adminPermission() {
    stubPlayerSenderWithPermissions(JACOB_UUID, Permission.ADMIN);
    awaitAsyncCommand(() -> onCommand(true, "1.3.4.5", "--shadow"));

    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully IP-banned 1.3.4.5"));
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
  }

  @Test
  public void playerSender_inGamePermission() {
    stubPlayerSenderWithPermissions(JACOB_UUID, Permission.IP_BAN_IN_GAME);
    awaitAsyncCommand(() -> onCommand(true, "1.3.4.5", "--duration=8h"));

    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully IP-banned 1.3.4.5"));
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
  }

  @Test
  public void playerSender_WebPermission() {
    stubPlayerSenderWithPermissions(JACOB_UUID, Permission.IP_BAN_WEB);
    awaitAsyncCommand(() -> onCommand(true, "1.3.4.5", "--duration=8h"));

    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully IP-banned 1.3.4.5"));
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
  }

  @Test
  public void playerSender_mediumLengthTooLong() {
    stubPlayerSenderWithPermissions(JACOB_UUID, Permission.IP_BAN_IN_GAME);
    awaitAsyncCommand(() -> onCommand(true, "1.3.4.5", "--duration=24h1m"));

    expectAbort(CommandAssertions.PUNISHMENT_TOO_LONG_MSG);
  }

  @Test
  public void playerSender_mediumLengthNotTooLong() {
    stubPlayerSenderWithPermissions(JACOB_UUID, Permission.IP_BAN_IN_GAME, Permission.PUNISHMENTS_LENGTH_MED);
    awaitAsyncCommand(() -> onCommand(true, "1.3.4.5", "--duration=24h1m"));

    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully IP-banned 1.3.4.5"));
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
  }

  @Test
  public void playerSender_longLengthTooLong() {
    stubPlayerSenderWithPermissions(JACOB_UUID, Permission.IP_BAN_IN_GAME, Permission.PUNISHMENTS_LENGTH_MED);
    awaitAsyncCommand(() -> onCommand(true, "1.3.4.5", "--duration=7d1m"));

    expectAbort(CommandAssertions.PUNISHMENT_TOO_LONG_MSG);
  }

  @Test
  public void playerSender_longLengthNotTooLong() {
    stubPlayerSenderWithPermissions(JACOB_UUID, Permission.IP_BAN_IN_GAME, Permission.PUNISHMENTS_LENGTH_LONG);
    awaitAsyncCommand(() -> onCommand(true, "1.3.4.5", "--duration=7d1m"));

    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully IP-banned 1.3.4.5"));
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
  }

  @Test
  public void playerSender_permanentLengthTooLong() {
    stubPlayerSenderWithPermissions(JACOB_UUID, Permission.IP_BAN_WEB, Permission.PUNISHMENTS_LENGTH_LONG);
    awaitAsyncCommand(() -> onCommand(true, "1.3.4.5"));

    expectAbort(CommandAssertions.NO_PERMANENT_PUNISHMENTS_MSG);
  }

  @Test
  public void playerSender_permanentLengthNotTooLong() {
    stubPlayerSenderWithPermissions(JACOB_UUID, Permission.IP_BAN_WEB, Permission.PUNISHMENTS_LENGTH_PERM);
    awaitAsyncCommand(() -> onCommand(true, "1.3.4.5"));

    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully IP-banned 1.3.4.5"));
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
  }

  @Test
  public void playerSender_noPermsForShadow() {
    stubPlayerSenderWithPermissions(JACOB_UUID, Permission.IP_BAN_WEB, Permission.PUNISHMENTS_LENGTH_PERM);
    awaitAsyncCommand(() -> onCommand(true, "1.3.4.5", "--shadow"));

    expectAbort(CommandAssertions.NO_SHADOW_PUNISHMENTS_MSG);
  }

  @Test
  public void playerSender_noPerms() {
    stubPlayerSenderWithPermissions(JACOB_UUID);
    onCommand(true, "1.3.4.5");

    expectAbort(CommandAssertions.NO_PERMS_MESSAGE);
  }

  @Test
  public void playerSender_noPermsServerSide() {
    sender = Mockito.mock(Player.class);
    Mockito.when(((Player) sender).getUniqueId()).thenReturn(TestConstants.PHYLLIS_UUID);
    Mockito.when(sender.hasPermission(Mockito.anyString())).thenReturn(true);
    awaitAsyncCommand(() -> onCommand(true, "1.3.4.5"));

    expectSingleMessage(TextConstants.INSUFFICIENT_PERMS);
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
  }

  @Test
  public void errorMessageFromServiceRequest() throws ApiException {
    PunishmentsApi punishmentsApi = Mockito.mock(PunishmentsApi.class);
    Mockito.when(mockClients.externalServer().getPunishmentsApi()).thenReturn(punishmentsApi);
    Mockito.when(punishmentsApi.createIpBan(Mockito.anyString(), Mockito.any())).thenThrow(new ApiException());
    config.setClientProvider(mockClients);

    awaitAsyncCommand(() -> onCommand(true, "1.2.3.4", "cheating"));
    Mockito.verify(sender).sendMessage(ChatColor.DARK_RED + "Unexpected plugin exception");
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(config.getLogging(), ONCE).logStackTrace(Mockito.any());
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
  public void successMessage_username_temp_withReason() {
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME, "cheating", "--duration=8h"));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully IP-banned " + IP_ADDRESS));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
    Mockito.verify(offender).kickPlayer(Mockito.contains("reason: cheating"));
    Mockito.verify(offender).kickPlayer(Mockito.contains("(8 hours from now)"));
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
  }

  @Test
  public void successMessage_ip_temp_withReason() {
    awaitAsyncCommand(() -> onCommand(true, IP_ADDRESS, "cheating", "--duration=8h"));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully IP-banned " + IP_ADDRESS));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
    Mockito.verify(offender).kickPlayer(Mockito.contains("reason: cheating"));
    Mockito.verify(offender).kickPlayer(Mockito.contains("(8 hours from now)"));
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
  }

  @Test
  public void successMessage_username_local_temp_withReason() {
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME, "cheating", "--local", "--duration=8h"));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully IP-banned " + IP_ADDRESS));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
    Mockito.verify(offender).kickPlayer(Mockito.contains("reason: cheating"));
    Mockito.verify(offender).kickPlayer(Mockito.contains("(8 hours from now)"));
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
  }

  @Test
  public void successMessage_ip_local_temp_withReason() {
    awaitAsyncCommand(() -> onCommand(true, IP_ADDRESS, "cheating", "--local", "--duration=8h"));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully IP-banned " + IP_ADDRESS));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
    Mockito.verify(offender).kickPlayer(Mockito.contains("reason: cheating"));
    Mockito.verify(offender).kickPlayer(Mockito.contains("(8 hours from now)"));
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
  }

  @Test
  public void successMessage_username_temp_noReason() {
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME, "--duration=8h"));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully IP-banned " + IP_ADDRESS));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
    Mockito.verify(offender).kickPlayer(Mockito.contains("reason: " + ChatColor.RESET));
    Mockito.verify(offender).kickPlayer(Mockito.contains("(8 hours from now)"));
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
  }

  @Test
  public void successMessage_ip_temp_noReason() {
    awaitAsyncCommand(() -> onCommand(true, IP_ADDRESS, "--duration=8h"));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully IP-banned " + IP_ADDRESS));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
  }

  @Test
  public void successMessage_username_local_temp_noReason() {
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME, "--local", "--duration=8h"));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully IP-banned " + IP_ADDRESS));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
  }

  @Test
  public void successMessage_ip_local_temp_noReason() {
    awaitAsyncCommand(() -> onCommand(true, IP_ADDRESS, "--local", "--duration=8h"));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully IP-banned " + IP_ADDRESS));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
  }

  @Test
  public void successMessage_username_perm_withReason() {
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME, "cheating"));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully IP-banned " + IP_ADDRESS));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
    Mockito.verify(offender).kickPlayer(Mockito.contains("reason: cheating"));
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
  }

  @Test
  public void successMessage_ip_perm_withReason() {
    awaitAsyncCommand(() -> onCommand(true, IP_ADDRESS, "cheating"));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully IP-banned " + IP_ADDRESS));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
    Mockito.verify(offender).kickPlayer(Mockito.contains("reason: cheating"));
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
  }

  @Test
  public void successMessage_username_local_perm_withReason() {
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME, "cheating", "--local"));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully IP-banned " + IP_ADDRESS));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
    Mockito.verify(offender).kickPlayer(Mockito.contains("reason: cheating"));
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
  }

  @Test
  public void successMessage_ip_local_perm_withReason() {
    awaitAsyncCommand(() -> onCommand(true, IP_ADDRESS, "cheating", "--local"));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully IP-banned " + IP_ADDRESS));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
  }

  @Test
  public void successMessage_username_perm_noReason() {
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully IP-banned " + IP_ADDRESS));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
    Mockito.verify(offender).kickPlayer(Mockito.contains("reason: " + ChatColor.RESET));
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
  }

  @Test
  public void successMessage_ip_perm_noReason() {
    awaitAsyncCommand(() -> onCommand(true, IP_ADDRESS));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully IP-banned " + IP_ADDRESS));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
    Mockito.verify(offender).kickPlayer(Mockito.contains("reason: " + ChatColor.RESET));
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
  }

  @Test
  public void successMessage_username_local_perm_noReason() {
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME, "--local"));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully IP-banned " + IP_ADDRESS));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
    Mockito.verify(offender).kickPlayer(Mockito.contains("reason: " + ChatColor.RESET));
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
  }

  @Test
  public void successMessage_ip_local_perm_noReason() {
    awaitAsyncCommand(() -> onCommand(true, IP_ADDRESS, "--local"));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully IP-banned " + IP_ADDRESS));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
    Mockito.verify(offender).kickPlayer(Mockito.contains("reason: " + ChatColor.RESET));
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
  }

  @Test
  public void visiblePunishmentBroadcastMessage_username_temp() {
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME, "cheating", "--broadcast", "--duration=8h"));
    Mockito.verify(config.getBukkitWrapper()).broadcastMessage(ChatColor.GRAY + "JacobCrofts has been banned");
    Mockito.verify(config.getBukkitWrapper(), ONCE).broadcastMessage(Mockito.any());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
    Mockito.verify(offender).kickPlayer(Mockito.contains("reason: cheating"));
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
  }

  @Test
  public void visiblePunishmentBroadcastMessage_ip_temp() {
    awaitAsyncCommand(() -> onCommand(true, IP_ADDRESS, "cheating", "--broadcast", "--duration=8h"));
    Mockito.verify(config.getBukkitWrapper()).broadcastMessage(ChatColor.GRAY + "JacobCrofts has been banned");
    Mockito.verify(config.getBukkitWrapper(), ONCE).broadcastMessage(Mockito.any());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
    Mockito.verify(offender).kickPlayer(Mockito.contains("reason: cheating"));
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
  }

  @Test
  public void visiblePunishmentBroadcastMessage_username_perm() {
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME, "cheating", "--broadcast"));
    Mockito.verify(config.getBukkitWrapper()).broadcastMessage(ChatColor.GRAY + "JacobCrofts has been banned");
    Mockito.verify(config.getBukkitWrapper(), ONCE).broadcastMessage(Mockito.any());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
    Mockito.verify(offender).kickPlayer(Mockito.contains("reason: cheating"));
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
  }

  @Test
  public void visiblePunishmentBroadcastMessage_ip_perm() {
    awaitAsyncCommand(() -> onCommand(true, IP_ADDRESS, "cheating", "--broadcast"));
    Mockito.verify(config.getBukkitWrapper()).broadcastMessage(ChatColor.GRAY + "JacobCrofts has been banned");
    Mockito.verify(config.getBukkitWrapper(), ONCE).broadcastMessage(Mockito.any());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
    Mockito.verify(offender).kickPlayer(Mockito.contains("reason: cheating"));
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
  }

  @Test
  public void invisiblePunishment_noBroadcastMessage_username() {
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME, "cheating"));
    Mockito.verify(config.getBukkitWrapper(), NEVER).broadcastMessage(Mockito.any());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
    Mockito.verify(offender).kickPlayer(Mockito.contains("reason: cheating"));
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
  }

  @Test
  public void invisiblePunishment_noBroadcastMessage_ip() {
    awaitAsyncCommand(() -> onCommand(true, IP_ADDRESS, "cheating"));
    Mockito.verify(config.getBukkitWrapper(), NEVER).broadcastMessage(Mockito.any());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
    Mockito.verify(offender).kickPlayer(Mockito.contains("reason: cheating"));
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
  }

  @Test
  public void bothVisibleAndShadowFlags_username() {
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME, "cheating", "--broadcast", "--shadow"));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully IP-banned " + IP_ADDRESS));
    Mockito.verify(sender).sendMessage(TextConstants.Commands.BROADCAST_FLAG_IGNORED_MESSAGE);
    Mockito.verify(sender, TWICE).sendMessage(Mockito.anyString());
    Mockito.verify(config.getBukkitWrapper(), NEVER).broadcastMessage(Mockito.any());
    Mockito.verify(offender).kickPlayer(KickMessages.SHADOW_BAN_MESSAGE);
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
  }

  @Test
  public void bothVisibleAndShadowFlags_ip() {
    awaitAsyncCommand(() -> onCommand(true, IP_ADDRESS, "cheating", "--broadcast", "--shadow"));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully IP-banned " + IP_ADDRESS));
    Mockito.verify(sender).sendMessage(TextConstants.Commands.BROADCAST_FLAG_IGNORED_MESSAGE);
    Mockito.verify(sender, TWICE).sendMessage(Mockito.anyString());
    Mockito.verify(config.getBukkitWrapper(), NEVER).broadcastMessage(Mockito.any());
    Mockito.verify(offender).kickPlayer(KickMessages.SHADOW_BAN_MESSAGE);
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
  }

  @Test
  public void bothVisibleAndPrivateFlags_username() {
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME, "cheating", "--broadcast", "--private"));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully IP-banned " + IP_ADDRESS));
    Mockito.verify(sender).sendMessage(TextConstants.Commands.BROADCAST_FLAG_IGNORED_MESSAGE);
    Mockito.verify(sender, TWICE).sendMessage(Mockito.anyString());
    Mockito.verify(config.getBukkitWrapper(), NEVER).broadcastMessage(Mockito.any());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
    Mockito.verify(offender).kickPlayer(Mockito.contains("reason: cheating"));
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
  }

  @Test
  public void bothVisibleAndPrivateFlags_ip() {
    awaitAsyncCommand(() -> onCommand(true, IP_ADDRESS, "cheating", "--broadcast", "--private"));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully IP-banned " + IP_ADDRESS));
    Mockito.verify(sender).sendMessage(TextConstants.Commands.BROADCAST_FLAG_IGNORED_MESSAGE);
    Mockito.verify(sender, TWICE).sendMessage(Mockito.anyString());
    Mockito.verify(config.getBukkitWrapper(), NEVER).broadcastMessage(Mockito.any());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
    Mockito.verify(offender).kickPlayer(Mockito.contains("reason: cheating"));
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
  }

  @Test
  public void bothShadowAndPrivateFlags_username() {
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME, "cheating", "--shadow", "--private"));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully IP-banned " + IP_ADDRESS));
    Mockito.verify(sender).sendMessage(TextConstants.Commands.PRIVATE_FLAG_REDUNDANT_MESSAGE);
    Mockito.verify(sender, TWICE).sendMessage(Mockito.anyString());
    Mockito.verify(config.getBukkitWrapper(), NEVER).broadcastMessage(Mockito.any());
    Mockito.verify(offender).kickPlayer(KickMessages.SHADOW_BAN_MESSAGE);
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
  }

  @Test
  public void bothShadowAndPrivateFlags_ip() {
    awaitAsyncCommand(() -> onCommand(true, IP_ADDRESS, "cheating", "--shadow", "--private"));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully IP-banned " + IP_ADDRESS));
    Mockito.verify(sender).sendMessage(TextConstants.Commands.PRIVATE_FLAG_REDUNDANT_MESSAGE);
    Mockito.verify(sender, TWICE).sendMessage(Mockito.anyString());
    Mockito.verify(config.getBukkitWrapper(), NEVER).broadcastMessage(Mockito.any());
    Mockito.verify(offender).kickPlayer(KickMessages.SHADOW_BAN_MESSAGE);
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
  }

  @Test
  public void visibleShadowAndPrivateFlags_username() {
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME, "cheating", "--broadcast", "--private", "--shadow"));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully IP-banned " + IP_ADDRESS));
    Mockito.verify(sender).sendMessage(TextConstants.Commands.BROADCAST_FLAG_IGNORED_MESSAGE);
    Mockito.verify(sender).sendMessage(TextConstants.Commands.PRIVATE_FLAG_REDUNDANT_MESSAGE);
    Mockito.verify(sender, Mockito.times(3)).sendMessage(Mockito.anyString());
    Mockito.verify(config.getBukkitWrapper(), NEVER).broadcastMessage(Mockito.any());
    Mockito.verify(offender).kickPlayer(KickMessages.SHADOW_BAN_MESSAGE);
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
  }

  @Test
  public void visibleShadowAndPrivateFlags_ip() {
    awaitAsyncCommand(() -> onCommand(true, IP_ADDRESS, "cheating", "--broadcast", "--private", "--shadow"));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully IP-banned " + IP_ADDRESS));
    Mockito.verify(sender).sendMessage(TextConstants.Commands.BROADCAST_FLAG_IGNORED_MESSAGE);
    Mockito.verify(sender).sendMessage(TextConstants.Commands.PRIVATE_FLAG_REDUNDANT_MESSAGE);
    Mockito.verify(sender, Mockito.times(3)).sendMessage(Mockito.anyString());
    Mockito.verify(config.getBukkitWrapper(), NEVER).broadcastMessage(Mockito.any());
    Mockito.verify(offender).kickPlayer(KickMessages.SHADOW_BAN_MESSAGE);
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
  }

  @Test
  public void successMessage_multiWordReason() {
    awaitAsyncCommand(() -> onCommand(true, IP_ADDRESS, "\"cheating", "and", "stuff\"", "etc"));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully IP-banned " + IP_ADDRESS));
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
  }

  @Test
  public void multipleAffectedPlayers_visible_username() {
    setMultipleOnlinePlayers();
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME, "--broadcast"));
    Mockito.verify(sender).sendMessage(ChatColor.YELLOW + "More than one online player will be affected by this IP ban");
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully IP-banned " + IP_ADDRESS));
    Mockito.verify(sender, TWICE).sendMessage(Mockito.anyString());
    Mockito.verify(config.getBukkitWrapper()).broadcastMessage(ChatColor.GRAY + "JacobCrofts has been banned");
    Mockito.verify(config.getBukkitWrapper()).broadcastMessage(ChatColor.GRAY + "AuntPhyllis has been banned");
    Mockito.verify(config.getBukkitWrapper(), TWICE).broadcastMessage(Mockito.any());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
    Mockito.verify(phyllis).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
    Mockito.verify(phyllis, ONCE).kickPlayer(Mockito.any());
    Mockito.verify(differentIpPlayer, NEVER).kickPlayer(Mockito.any());
    Mockito.verify(nullIpPlayer, NEVER).kickPlayer(Mockito.any());
  }

  @Test
  public void multipleAffectedPlayers_visible_ip() {
    setMultipleOnlinePlayers();
    awaitAsyncCommand(() -> onCommand(true, IP_ADDRESS, "--broadcast"));
    Mockito.verify(sender).sendMessage(ChatColor.YELLOW + "More than one online player will be affected by this IP ban");
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully IP-banned " + IP_ADDRESS));
    Mockito.verify(sender, TWICE).sendMessage(Mockito.anyString());
    Mockito.verify(config.getBukkitWrapper()).broadcastMessage(ChatColor.GRAY + "JacobCrofts has been banned");
    Mockito.verify(config.getBukkitWrapper()).broadcastMessage(ChatColor.GRAY + "AuntPhyllis has been banned");
    Mockito.verify(config.getBukkitWrapper(), TWICE).broadcastMessage(Mockito.any());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
    Mockito.verify(phyllis).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
    Mockito.verify(phyllis, ONCE).kickPlayer(Mockito.any());
    Mockito.verify(differentIpPlayer, NEVER).kickPlayer(Mockito.any());
    Mockito.verify(nullIpPlayer, NEVER).kickPlayer(Mockito.any());
  }

  @Test
  public void multipleAffectedPlayers_invisible_username() {
    setMultipleOnlinePlayers();
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME));
    Mockito.verify(sender).sendMessage(ChatColor.YELLOW + "More than one online player will be affected by this IP ban");
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully IP-banned " + IP_ADDRESS));
    Mockito.verify(sender, TWICE).sendMessage(Mockito.anyString());
    Mockito.verify(config.getBukkitWrapper(), NEVER).broadcastMessage(Mockito.any());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
    Mockito.verify(phyllis).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
    Mockito.verify(phyllis, ONCE).kickPlayer(Mockito.any());
    Mockito.verify(differentIpPlayer, NEVER).kickPlayer(Mockito.any());
    Mockito.verify(nullIpPlayer, NEVER).kickPlayer(Mockito.any());
  }

  @Test
  public void multipleAffectedPlayers_invisible_ip() {
    setMultipleOnlinePlayers();
    awaitAsyncCommand(() -> onCommand(true, IP_ADDRESS));
    Mockito.verify(sender).sendMessage(ChatColor.YELLOW + "More than one online player will be affected by this IP ban");
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully IP-banned " + IP_ADDRESS));
    Mockito.verify(sender, TWICE).sendMessage(Mockito.anyString());
    Mockito.verify(config.getBukkitWrapper(), NEVER).broadcastMessage(Mockito.any());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
    Mockito.verify(phyllis).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
    Mockito.verify(phyllis, ONCE).kickPlayer(Mockito.any());
    Mockito.verify(differentIpPlayer, NEVER).kickPlayer(Mockito.any());
    Mockito.verify(nullIpPlayer, NEVER).kickPlayer(Mockito.any());
  }

  @Test
  public void rangeIpBan() {
    setMultipleOnlinePlayers();
    awaitAsyncCommand(() -> onCommand(true, IP_ADDRESS_RANGE_COVERING_PLAYER));
    Mockito.verify(sender).sendMessage(Mockito.contains("Successfully IP-banned " + IP_ADDRESS_RANGE_COVERING_PLAYER));
    Mockito.verify(sender).sendMessage(ChatColor.YELLOW + "More than one online player will be affected by this IP ban");
    Mockito.verify(sender, TWICE).sendMessage(Mockito.anyString());
    Mockito.verify(offender).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
    Mockito.verify(offender).kickPlayer(Mockito.contains("reason: " + ChatColor.RESET));
    Mockito.verify(offender).kickPlayer(Mockito.contains("expires: never"));
    Mockito.verify(offender, ONCE).kickPlayer(Mockito.any());
    Mockito.verify(phyllis).kickPlayer(Mockito.contains(KickMessages.BAN_MESSAGE));
    Mockito.verify(phyllis).kickPlayer(Mockito.contains("reason: " + ChatColor.RESET));
    Mockito.verify(phyllis).kickPlayer(Mockito.contains("expires: never"));
    Mockito.verify(phyllis, ONCE).kickPlayer(Mockito.any());
    Mockito.verify(differentIpPlayer, NEVER).kickPlayer(Mockito.anyString());
    Mockito.verify(nullIpPlayer, NEVER).kickPlayer(Mockito.anyString());
  }


  private void setMultipleOnlinePlayers() {
    // one player with the same ip address
    phyllis = newStubbedPlayer(PHYLLIS_USERNAME, PHYLLIS_UUID, true);
    mockIpAddress(phyllis, IP_ADDRESS);

    // one player with a different ip address
    differentIpPlayer = newStubbedPlayer("nobody_cares", UUID.randomUUID(), true);
    mockIpAddress(differentIpPlayer, "23.24.25.26");

    // one player with a null ip address
    nullIpPlayer = newStubbedPlayer("nobody_cares2", UUID.randomUUID(), true);
    Mockito.when(nullIpPlayer.getAddress()).thenReturn(null);
  }

  private void mockIpAddress(Player player, String address) {
    InetSocketAddress socketAddress = Mockito.mock(InetSocketAddress.class);
    InetAddress inetAddress = Mockito.mock(InetAddress.class);
    Mockito.when(socketAddress.getAddress()).thenReturn(inetAddress);
    Mockito.when(inetAddress.getHostAddress()).thenReturn(address);
    Mockito.when(player.getAddress()).thenReturn(socketAddress);
  }

  private void onCommand(boolean noSyntaxError, String... args) {
    CommandExecutorAsync cmd = new CmdBanIp(config);
    Assert.assertEquals(noSyntaxError, cmd.onCommand(sender, command, "", args));
  }
}
