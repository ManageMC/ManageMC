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
import lombok.Getter;
import lombok.Setter;

public class SpigotPluginConfigLocal implements SpigotPluginConfig {

  @Getter
  @Setter
  private ClientProvider clientProvider;
  @Getter
  @Setter
  private PlayerUuidResolver uuidResolver;
  @Getter
  @Setter
  private BukkitLogging logging;
  @Getter
  @Setter
  private BukkitWrapper bukkitWrapper;

  // state
  @Getter
  @Setter
  private LocalConfig localConfig;
  @Getter
  @Setter
  private MuteManager muteManager;
  @Getter
  @Setter
  private PlayerData playerData;
  @Getter
  @Setter
  private PermissionsManager permissionsManager;
  @Getter
  @Setter
  private ServerGroupsData serverGroupsData;
  @Getter
  @Setter
  private PardonablePunishmentData pardonablePunishmentData;

  // services
  @Getter
  @Setter
  private ApiPingService apiPingService;
  @Getter
  @Setter
  private HeartbeatService heartbeatService;
  @Getter
  @Setter
  private PunishmentHistoryService punishmentHistoryService;
  @Getter
  @Setter
  private PardonService pardonService;
  @Getter
  @Setter
  private RemoteConfigService remoteConfigService;
  @Getter
  @Setter
  private BanService banService;
  @Getter
  @Setter
  private MuteService muteService;
  @Getter
  @Setter
  private WarningService warningService;
  @Getter
  @Setter
  private IpBanService ipBanService;
  @Getter
  @Setter
  private LoginLogoutService loginLogoutService;
  @Getter
  @Setter
  private WatchlistService watchlistService;
  @Getter
  @Setter
  private ReportsService reportsService;
  @Getter
  @Setter
  private NoteService noteService;
  @Getter
  @Setter
  private CreatePlayerService createPlayerService;

  // tasks
  @Getter
  @Setter
  private PeriodicHeartbeatSender periodicHeartbeatSender;

  public SpigotPluginConfigLocal(BukkitLogging logging, BukkitWrapper bukkitWrapper, LocalConfig localConfig) {
    this(
        logging,
        bukkitWrapper,
        localConfig,
        new PlayerData(),
        ClientProvider.demo(newLogger(logging), localConfig.getKeys(), localConfig.getServerGroup())
    );
  }

  protected SpigotPluginConfigLocal(
      BukkitLogging logging,
      BukkitWrapper bukkitWrapper,
      LocalConfig localConfig,
      PlayerData playerData,
      ClientProvider clientProvider
  ) {
    this.clientProvider = clientProvider;
    this.uuidResolver = new PlayerUuidResolver(logging, bukkitWrapper);
    this.logging = logging;
    this.bukkitWrapper = bukkitWrapper;

    // state
    this.localConfig = localConfig;
    this.muteManager = new MuteManager();
    this.playerData = playerData;
    this.permissionsManager = new PermissionsManager(logging, bukkitWrapper);
    this.serverGroupsData = new ServerGroupsData(clientProvider);
    this.pardonablePunishmentData = new PardonablePunishmentData();

    // services
    this.apiPingService = new ApiPingService(this);
    this.heartbeatService = new HeartbeatService(this);
    this.punishmentHistoryService = new PunishmentHistoryService(this);
    this.pardonService = new PardonService(this);
    this.remoteConfigService = new RemoteConfigService(clientProvider);
    this.banService = new BanService(this);
    this.muteService = new MuteService(this);
    this.warningService = new WarningService(this);
    this.ipBanService = new IpBanService(this);
    this.loginLogoutService = new LoginLogoutService(this);
    this.noteService = new NoteService(this);
    this.createPlayerService = new CreatePlayerService(this);
    this.watchlistService = new WatchlistService(clientProvider);
    this.reportsService = new ReportsService(this);

    // tasks
    this.periodicHeartbeatSender = new PeriodicHeartbeatSender(logging, bukkitWrapper, heartbeatService);
  }

  private static ClientProviderLogger newLogger(BukkitLogging logging) {
    return new ClientProviderLogger(logging);
  }

  private static class ClientProviderLogger implements ClientProvider.Logger {

    private final BukkitLogging logging;

    private ClientProviderLogger(BukkitLogging logging) {
      this.logging = logging;
    }

    @Override
    public void logWarning(String s) {
      logging.logWarning(s);
    }

    @Override
    public void logInfo(String s) {
      logging.logInfo(s);
    }
  }
}
