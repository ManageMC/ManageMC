package com.managemc.importer.command.handler;

import com.managemc.importer.command.base.AsyncCommandHandler;
import com.managemc.importer.command.base.CommandAssertions;
import com.managemc.importer.command.base.CommandValidationException;
import com.managemc.importer.config.ManageMCImportPluginConfig;
import com.managemc.importer.service.PunishmentImporterService;
import com.managemc.plugins.bukkit.BukkitWrapper;
import org.bukkit.command.CommandSender;

import java.io.File;

public class CmdImportHandler extends AsyncCommandHandler {

  private final BukkitWrapper bukkitWrapper;
  private final PunishmentImporterService importerService;
  private final String advancedBanMainClass;
  private final String maxBansMainClass;

  private CmdImportHandler(
      CommandSender sender,
      String[] args,
      BukkitWrapper bukkitWrapper,
      PunishmentImporterService importerService,
      String advancedBanMainClass,
      String maxBansMainClass
  ) {
    super(sender, args);
    this.bukkitWrapper = bukkitWrapper;
    this.importerService = importerService;
    this.advancedBanMainClass = advancedBanMainClass;
    this.maxBansMainClass = maxBansMainClass;
  }

  public static CmdImportHandler defaultInstance(
      CommandSender sender,
      String[] args,
      ManageMCImportPluginConfig config
  ) {
    return new CmdImportHandler(
        sender,
        args,
        config.getBukkitWrapper(),
        config.getImporterService(),
        config.getAdvancedBanMainClass(),
        config.getMaxBansMainClass()
    );
  }

  private PunishmentSource source;

  @Override
  public void preProcessCommand() {
    CommandAssertions.assertArgsLength(args, 1);
    source = CommandAssertions.assertOneOf(args[0], PunishmentSource.class);
    validateSoftDependencies(source);
  }

  @Override
  public void processCommandAsync() {
    switch (source) {
      case VANILLA:
        importerService.importVanilla(sender);
        return;
      case ADVANCED_BAN:
        importerService.importAdvancedBan(sender);
        return;
      case MAX_BANS_PLUS:
        importerService.importMaxBans(sender);
        return;
      case ESSENTIALS_X:
        File dataFolder = bukkitWrapper.getOtherPlugin("Essentials").getDataFolder();
        importerService.importEssentialsX(sender, dataFolder);
        return;
    }

    throw new RuntimeException("Unexpected punishment source: " + source);
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
    if (bukkitWrapper.getOtherPlugin(plugin) == null) {
      String message = String.format("Plugin expected but not found: %s", plugin);
      throw new CommandValidationException(message, false);
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
      throw new CommandValidationException(message, false);
    }
  }

  public enum PunishmentSource {
    VANILLA,
    ADVANCED_BAN,
    MAX_BANS_PLUS,
    ESSENTIALS_X
  }
}
