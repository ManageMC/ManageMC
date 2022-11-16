package com.managemc.importer;

import com.managemc.api.ApiException;
import com.managemc.api.wrapper.refresher.TokenRefresher;
import com.managemc.importer.command.CmdImport;
import com.managemc.importer.command.CmdJob;
import com.managemc.importer.command.base.CommandBase;
import com.managemc.importer.config.LocalConfig;
import com.managemc.importer.config.ManageMCImportPluginConfig;
import com.managemc.plugins.bukkit.BukkitWrapper;
import com.managemc.plugins.config.FlexibleLocalConfigLoader;
import com.managemc.plugins.config.LocalConfigLoader;
import com.managemc.plugins.logging.BukkitLogging;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.Optional;

@SuppressWarnings("unused")
public class ManageMCImporterPlugin extends JavaPlugin {

  private static final String DEFAULT_CONFIG_FILENAME = "default-config.yml";
  private static final String CONFIG_FILENAME = "ManageMC.yml";
  private static final String PLUGIN_NAME = "ManageMC Importer";

  @Override
  public void onEnable() {
    BukkitWrapper bukkitWrapper = new BukkitWrapper(this);
    BukkitLogging logger = new BukkitLogging(PLUGIN_NAME);

    String configFilePath = bukkitWrapper.getDataFolder() + "/" + CONFIG_FILENAME;

    try {
      LocalConfigLoader configLoader = new FlexibleLocalConfigLoader(
          new String[]{LocalConfig.PUBLIC_KEY_KEY, LocalConfig.PRIVATE_KEY_KEY},
          bukkitWrapper.getDataFolder(),
          CONFIG_FILENAME,
          Optional
              .ofNullable(bukkitWrapper.getResource(DEFAULT_CONFIG_FILENAME))
              .orElseThrow(() -> new RuntimeException("Default config file not found. This is a bug."))
      );

      LocalConfig localConfig = new LocalConfig(configLoader.load());
      ManageMCImportPluginConfig config = new ManageMCImportPluginConfig(logger, localConfig);

      registerCommand(new CmdImport(logger, config));
      registerCommand(new CmdJob(logger, config.getJobStatusService()));

      config.getClientProvider().externalApplication().getPingApi().ping();
    } catch (LocalConfig.IncompleteConfigException e) {
      logger.logInfo("Welcome to ManageMC! Please fill out the local config file at " + configFilePath + ".");
      logger.logInfo("Shutting down because local config is incomplete...");
      bukkitWrapper.disable();
    } catch (TokenRefresher.BadCredentialsException e) {
      logger.logWarning("Authentication with ManageMC failed because the credentials at " + configFilePath + " are wrong.");
      logger.logWarning("Shutting down due to misconfiguration...");
      bukkitWrapper.disable();
    } catch (RuntimeException | ApiException e) {
      logger.logStackTrace(e);
      logger.logWarning("Shutting down due to an unexpected error...");
      bukkitWrapper.disable();
    }
  }

  @SneakyThrows
  private void registerCommand(CommandBase command) {
    final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
    bukkitCommandMap.setAccessible(true);
    CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
    commandMap.register(PLUGIN_NAME, command);
  }
}
