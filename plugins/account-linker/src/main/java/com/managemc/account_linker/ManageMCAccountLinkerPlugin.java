package com.managemc.account_linker;

import com.managemc.plugins.bukkit.BukkitWrapper;
import com.managemc.plugins.config.FileBasedLocalConfigLoader;
import com.managemc.plugins.config.LocalConfigLoader;
import com.managemc.plugins.logging.BukkitLogging;
import com.managemc.account_linker.command.CmdLinkAccount;
import com.managemc.account_linker.config.AccountLinkerConfig;
import com.managemc.account_linker.config.AccountLinkerConfigLocal;
import com.managemc.account_linker.model.LocalConfig;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("unused")
public class ManageMCAccountLinkerPlugin extends JavaPlugin {

  private static final String DEFAULT_CONFIG_FILENAME = "default-config.yml";

  @Override
  public void onEnable() {
    BukkitWrapper bukkitWrapper = new BukkitWrapper(this);
    BukkitLogging logger = new BukkitLogging("ManageMC Internal");

    try {
      LocalConfigLoader configLoader = new FileBasedLocalConfigLoader(
          bukkitWrapper.getDataFolder(),
          "config.yml",
          Optional
              .ofNullable(bukkitWrapper.getResource(DEFAULT_CONFIG_FILENAME))
              .orElseThrow(() -> new RuntimeException("Default config file not found. This is a bug."))
      );

      LocalConfig localConfig = new LocalConfig(configLoader.load());
      AccountLinkerConfig config = new AccountLinkerConfigLocal(logger, localConfig);

      config.getApiPingService().ping();

      Objects
          .requireNonNull(getCommand("mmc"))
          .setExecutor(new CmdLinkAccount(logger, config.getAccountLinkingService()));
    } catch (Exception e) {
      logger.logStackTrace(e);
      bukkitWrapper.shutdown();
    }
  }
}
