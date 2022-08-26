package com.managemc.importer;

import com.managemc.api.ApiException;
import com.managemc.importer.command.CmdImport;
import com.managemc.importer.command.CmdJob;
import com.managemc.importer.command.base.CommandBase;
import com.managemc.importer.config.LocalConfig;
import com.managemc.importer.config.ManageMCImportPluginConfig;
import com.managemc.plugins.bukkit.BukkitWrapper;
import com.managemc.plugins.config.FileBasedLocalConfigLoader;
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
    BukkitLogging logging = new BukkitLogging(PLUGIN_NAME);

    try {
      LocalConfigLoader configLoader = new FileBasedLocalConfigLoader(
          bukkitWrapper.getDataFolder(),
          CONFIG_FILENAME,
          Optional
              .ofNullable(bukkitWrapper.getResource(DEFAULT_CONFIG_FILENAME))
              .orElseThrow(() -> new RuntimeException("Default config file not found. This is a bug."))
      );

      LocalConfig localConfig = new LocalConfig(configLoader.load());
      ManageMCImportPluginConfig config = new ManageMCImportPluginConfig(logging, localConfig);

      registerCommand(new CmdImport(logging, config));
      registerCommand(new CmdJob(logging, config.getJobStatusService()));

      config.getClientProvider().externalApplication().getPingApi().ping();
    } catch (RuntimeException | ApiException e) {
      logging.logStackTrace(e);
      bukkitWrapper.shutdown();
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
