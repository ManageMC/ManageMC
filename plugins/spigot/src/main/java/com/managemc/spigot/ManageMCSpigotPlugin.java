package com.managemc.spigot;

import com.managemc.api.ApiException;
import com.managemc.api.wrapper.refresher.TokenRefresher;
import com.managemc.plugins.bukkit.BukkitWrapper;
import com.managemc.plugins.config.FlexibleLocalConfigLoader;
import com.managemc.plugins.config.LocalConfigLoader;
import com.managemc.plugins.logging.BukkitLogging;
import com.managemc.spigot.command.processor.*;
import com.managemc.spigot.config.SpigotPluginConfig;
import com.managemc.spigot.config.SpigotPluginConfigLocal;
import com.managemc.spigot.config.helper.VersionDiscerner;
import com.managemc.spigot.config.model.LocalConfig;
import com.managemc.spigot.listener.Listeners;
import com.managemc.spigot.util.BatchKickPlayersOnDisable;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("unused")
public class ManageMCSpigotPlugin extends JavaPlugin {

  private static final String DEFAULT_CONFIG_FILENAME = "default-config.yml";
  private static final String CONFIG_FILENAME = "ManageMC.yml";
  private static final String[] KEYS = new String[]{LocalConfig.PUBLIC_KEY_KEY, LocalConfig.PRIVATE_KEY_KEY, LocalConfig.SERVER_GROUP_KEY, LocalConfig.SERVER_NAME_KEY};

  private SpigotPluginConfig config;

  @Override
  public void onEnable() {
    BukkitLogging logger = new BukkitLogging("ManageMC");
    BukkitWrapper bukkitWrapper = new BukkitWrapper(this);

    String configFilePath = bukkitWrapper.getDataFolder() + "/" + CONFIG_FILENAME;

    try {
      config = buildConfig(logger, bukkitWrapper);

      config.getApiPingService().ping();
      config.getRemoteConfigService().loadSync();
      config.getHeartbeatService().emitInitialHeartbeat();

      registerListeners(config);
      registerCommands(config);
      config.getPeriodicHeartbeatSender().start();
    } catch (LocalConfig.IncompleteConfigException e) {
      logger.logInfo("Welcome to ManageMC! Please fill out the local config file at " + configFilePath + ".");
      logger.logInfo("Unloading ManageMC because local config is incomplete...");
      bukkitWrapper.disable();
    } catch (TokenRefresher.BadCredentialsException e) {
      logger.logWarning("Authentication with ManageMC failed because the credentials at " + configFilePath + " are wrong.");
      logger.logWarning("Unloading ManageMC due to misconfiguration...");
      bukkitWrapper.disable();
    } catch (RuntimeException | ApiException | IOException e) {
      logger.logStackTrace(e);
      logger.logWarning("Unloading ManageMC down due to an unexpected error...");
      bukkitWrapper.disable();
    }
  }

  @Override
  public void onDisable() {
    if (config == null) {
      return;
    }

    try {
      new BatchKickPlayersOnDisable(config.getLogging(), config.getBukkitWrapper(), config.getLoginLogoutService())
          .run();

      if (config.getPeriodicHeartbeatSender() != null) {
        config.getPeriodicHeartbeatSender().stop();
      }

      LocalConfig.DynamicComponents dynamicComponents = config.getLocalConfig().getDynamicComponents();
      if (dynamicComponents != null) {
        config.getHeartbeatService().emitFinalHeartbeat();
      }
    } catch (RuntimeException | ApiException e) {
      config.getLogging().logWarning("Something unexpected went wrong while disabling ManageMC.");
      config.getLogging().logStackTrace(e);
    }
  }

  private SpigotPluginConfig buildConfig(BukkitLogging logging, BukkitWrapper bukkitWrapper) throws IOException {
    String version = new VersionDiscerner(logging).determineVersion();

    LocalConfigLoader configLoader = new FlexibleLocalConfigLoader(
        KEYS,
        bukkitWrapper.getDataFolder(),
        CONFIG_FILENAME,
        Optional
            .ofNullable(bukkitWrapper.getResource(DEFAULT_CONFIG_FILENAME))
            .orElseThrow(() -> new RuntimeException("Default config file not found. This is a bug."))
    );

    return new SpigotPluginConfigLocal(logging, bukkitWrapper, new LocalConfig(configLoader.load(), version));
  }

  private void registerListeners(SpigotPluginConfig config) {
    Listeners listeners = new Listeners(config);
    config.getBukkitWrapper().registerListener(listeners);
  }

  private void registerCommands(SpigotPluginConfig config) {
    registerCommand("report", new CmdReport(config));
    registerCommand("create-player", new CmdCreatePlayer(config));
    registerCommand("ban", new CmdBan(config));
    registerCommand("mute", new CmdMute(config));
    registerCommand("warn", new CmdWarn(config));
    registerCommand("ban-ip", new CmdBanIp(config));
    registerCommand("history", new CmdHistory(config));
    registerCommand("pardon", new CmdPardon(config));
    registerCommand("watchlist", new CmdWatchlist(config));
    registerCommand("watch", new CmdWatch(config));
    registerCommand("unwatch", new CmdUnwatch(config));
  }

  private <T extends TabCompleter & CommandExecutor> void registerCommand(String cmd, T executor) {
    PluginCommand command = Objects.requireNonNull(getCommand(cmd));
    command.setExecutor(executor);
    command.setTabCompleter(executor);
  }
}
