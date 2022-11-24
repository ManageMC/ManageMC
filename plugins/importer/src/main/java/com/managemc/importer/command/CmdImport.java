package com.managemc.importer.command;

import com.managemc.importer.command.util.CommandAssertions;
import com.managemc.importer.config.ManageMCImportPluginConfig;
import com.managemc.plugins.command.AbortCommand;
import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.plugins.logging.BukkitLogging;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.util.List;

public class CmdImport extends CommandExecutorAsync {

  private final ManageMCImportPluginConfig config;
  private final String advancedBanMainClass;
  private final String maxBansMainClass;

  public CmdImport(BukkitLogging logging, ManageMCImportPluginConfig config) {
    this(logging, config, config.getAdvancedBanMainClass(), config.getMaxBansMainClass());
  }

  private CmdImport(
      BukkitLogging logging,
      ManageMCImportPluginConfig config,
      String advancedBanMainClass,
      String maxBansMainClass
  ) {
    super(logging);
    this.config = config;
    this.advancedBanMainClass = advancedBanMainClass;
    this.maxBansMainClass = maxBansMainClass;
  }

  private PunishmentSource source;

  @Override
  protected void preProcessCommand(CommandSender sender, String[] args) {
    CommandAssertions.assertArgsLength(args, 1);
    source = CommandAssertions.assertOneOf(args[0], PunishmentSource.class);
    validateSoftDependencies(source);
  }

  @Override
  protected void onCommandAsync(CommandSender sender) {
    switch (source) {
      case VANILLA:
        config.getImporterService().importVanilla(sender);
        return;
      case ADVANCED_BAN:
        config.getImporterService().importAdvancedBan(sender);
        return;
      case MAX_BANS_PLUS:
        config.getImporterService().importMaxBans(sender);
        return;
      case ESSENTIALS_X:
        File dataFolder = config.getBukkitWrapper().getOtherPlugin("Essentials").getDataFolder();
        config.getImporterService().importEssentialsX(sender, dataFolder);
        return;
    }

    throw new RuntimeException("Unexpected punishment source: " + source);
  }

  @Override
  protected List<String> onTabComplete(CommandSender sender, String[] args) {
    // TODO
    return null;
  }

  private void validateSoftDependencies(PunishmentSource source) {
    switch (source) {
      case VANILLA:
        break;
      case ADVANCED_BAN:
        assertSoftDependencyFound(
            "AdvancedBan",
            "AdvancedBan-Bundle-2.3.0-RELEASE.jar",
            advancedBanMainClass
        );
        break;
      case MAX_BANS_PLUS:
        assertSoftDependencyFound(
            "MaxBansPlus",
            "maxbans-plus-2.0.jar",
            maxBansMainClass
        );
        break;
      case ESSENTIALS_X:
        assertPresenceOfOtherPlugin("Essentials");
        break;
    }
  }

  private void assertPresenceOfOtherPlugin(String plugin) {
    if (config.getBukkitWrapper().getOtherPlugin(plugin) == null) {
      String message = String.format("Plugin expected but not found: %s", plugin);
      throw AbortCommand.withoutUsageMessage(message);
    }
  }

  private void assertSoftDependencyFound(String pluginName, String jarName, String className) {
    try {
      Class.forName(className);
    } catch (ClassNotFoundException e) {
      String message = String.format(
          "Unable to find the %s plugin running locally. The jar we tested with is " +
              "called %s, and the class we're looking for is %s. If you are using a different " +
              "version of %s, please update (or remind us to do so!)",
          pluginName, jarName, className, pluginName
      );
      throw AbortCommand.withoutUsageMessage(message);
    }
  }

  public enum PunishmentSource {
    VANILLA,
    ADVANCED_BAN,
    MAX_BANS_PLUS,
    ESSENTIALS_X
  }
}
