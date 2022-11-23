package com.managemc.spigot.config;

import com.managemc.api.wrapper.ClientProvider;
import com.managemc.plugins.bukkit.BukkitWrapper;
import com.managemc.plugins.logging.BukkitLogging;
import com.managemc.spigot.command.util.punishments.PlayerUuidResolver;
import com.managemc.spigot.config.model.LocalConfig;
import com.managemc.spigot.config.model.PermissionsManager;
import com.managemc.spigot.service.*;
import com.managemc.spigot.state.MuteManager;
import com.managemc.spigot.state.PardonablePunishmentData;
import com.managemc.spigot.state.PlayerData;
import com.managemc.spigot.state.ServerGroupsData;
import com.managemc.spigot.task.PeriodicHeartbeatSender;

public interface SpigotPluginConfig {

  ClientProvider getClientProvider();

  PlayerUuidResolver getUuidResolver();

  ApiPingService getApiPingService();

  HeartbeatService getHeartbeatService();

  PunishmentHistoryService getPunishmentHistoryService();

  PardonService getPardonService();

  RemoteConfigService getRemoteConfigService();

  BanService getBanService();

  MuteService getMuteService();

  WarningService getWarningService();

  IpBanService getIpBanService();

  MuteManager getMuteManager();

  PeriodicHeartbeatSender getPeriodicHeartbeatSender();

  LoginLogoutService getLoginLogoutService();

  WatchlistService getWatchlistService();

  ReportsService getReportsService();

  PlayerData getPlayerData();

  PermissionsManager getPermissionsManager();

  NoteService getNoteService();

  CreatePlayerService getCreatePlayerService();

  ServerGroupsData getServerGroupsData();

  PardonablePunishmentData getPardonablePunishmentData();

  LocalConfig getLocalConfig();

  BukkitLogging getLogging();

  BukkitWrapper getBukkitWrapper();
}
