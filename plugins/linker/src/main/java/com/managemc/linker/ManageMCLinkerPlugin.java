package com.managemc.linker;

import com.managemc.api.ApiException;
import com.managemc.api.wrapper.refresher.TokenRefresher;
import com.managemc.linker.command.CmdLinkAccount;
import com.managemc.linker.config.AccountLinkerConfig;
import com.managemc.linker.config.AccountLinkerConfigLocal;
import com.managemc.linker.model.LocalConfig;
import com.managemc.plugins.bukkit.BukkitWrapper;
import com.managemc.plugins.config.FileBasedLocalConfigLoader;
import com.managemc.plugins.config.LocalConfigLoader;
import com.managemc.plugins.logging.BukkitLogging;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("unused")
public class ManageMCLinkerPlugin extends JavaPlugin {

  private static final String DEFAULT_CONFIG_FILENAME = "default-config.yml";
  private static final String CONFIG_FILENAME = "config.yml";

  @Override
  public void onEnable() {
    BukkitWrapper bukkitWrapper = new BukkitWrapper(this);
    BukkitLogging logger = new BukkitLogging("ManageMC Linker");

    String configFilePath = bukkitWrapper.getDataFolder() + "/" + CONFIG_FILENAME;

    try {
      LocalConfigLoader configLoader = new FileBasedLocalConfigLoader(
          bukkitWrapper.getDataFolder(),
          CONFIG_FILENAME,
          Optional
              .ofNullable(bukkitWrapper.getResource(DEFAULT_CONFIG_FILENAME))
              .orElseThrow(() -> new RuntimeException("Default config file not found. This is a bug."))
      );

      LocalConfig localConfig = new LocalConfig(configLoader.load());
      AccountLinkerConfig config = new AccountLinkerConfigLocal(logger, localConfig);

      config.getApiPingService().ping();

      Objects
          .requireNonNull(getCommand("link"))
          .setExecutor(new CmdLinkAccount(logger, config.getAccountLinkingService()));
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
}
