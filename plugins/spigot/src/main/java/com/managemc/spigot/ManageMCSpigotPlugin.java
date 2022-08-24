package com.managemc.spigot;

import com.managemc.api.ApiException;
import com.managemc.plugins.bukkit.BukkitWrapper;
import com.managemc.plugins.config.FileBasedLocalConfigLoader;
import com.managemc.plugins.config.LocalConfigLoader;
import com.managemc.plugins.logging.BukkitLogging;
import com.managemc.spigot.command.processor.CmdMMC;
import com.managemc.spigot.command.util.CommandNodeMap;
import com.managemc.spigot.config.SpigotPluginConfig;
import com.managemc.spigot.config.SpigotPluginConfigLocal;
import com.managemc.spigot.config.helper.VersionDiscerner;
import com.managemc.spigot.config.model.LocalConfig;
import com.managemc.spigot.listener.Listeners;
import com.managemc.spigot.util.BatchKickPlayersOnDisable;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("unused")
public class ManageMCSpigotPlugin extends JavaPlugin {

  private static final String DEFAULT_CONFIG_FILENAME = "default-config.yml";

  private SpigotPluginConfig config;

  @Override
  public void onEnable() {
    BukkitLogging logging = new BukkitLogging("ManageMC");
    BukkitWrapper wrapper = new BukkitWrapper(this);

    try {
      config = buildConfig(logging, wrapper);

      config.getApiPingService().ping();
      config.getRemoteConfigService().loadSync();
      config.getHeartbeatService().emitInitialHeartbeat();

      registerListeners(config);
      registerCommands(config);
      config.getPeriodicHeartbeatSender().start();
    } catch (Exception e) {
      logging.logStackTrace(e);
      logging.logSevere("Since ManageMC failed to load, the server will shut down");
      getServer().shutdown();
    }
  }

  @Override
  public void onDisable() {
    try {
      new BatchKickPlayersOnDisable(config.getLogging(), config.getBukkitWrapper(), config.getLoginLogoutService())
          .run();

      if (config.getPeriodicHeartbeatSender() != null) {
        config.getPeriodicHeartbeatSender().stop();
      }
      config.getHeartbeatService().emitFinalHeartbeat();
    } catch (ApiException e) {
      config.getLogging().logStackTrace(e);
      config.getLogging().logSevere("Something unexpected went wrong while disabling ManageMC.");
    }
  }

  private SpigotPluginConfig buildConfig(BukkitLogging logging, BukkitWrapper bukkitWrapper) throws IOException {
    String version = new VersionDiscerner(logging).determineVersion();

    LocalConfigLoader configLoader = new FileBasedLocalConfigLoader(
        bukkitWrapper.getDataFolder(),
        "ManageMC.yml",
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
    PluginCommand command = Objects.requireNonNull(getCommand("mmc"));
    command.setExecutor(new CmdMMC(config));
    command.setTabCompleter(new CommandNodeMap(config));
  }
}
