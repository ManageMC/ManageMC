package com.managemc.importer.command;

import com.managemc.importer.command.base.CommandAssertions;
import com.managemc.importer.config.ManageMCImportPluginConfig;
import com.managemc.plugins.logging.BukkitLogging;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

public class CmdImportTest {

  private static final String USAGE_MESSAGE = ChatColor.RED + "Usage: " + CmdImport.USAGE;

  private CommandSender sender;

  @Before
  public void setup() {
    sender = Mockito.mock(CommandSender.class);
  }

  @Test
  public void noArgs() {
    onCommand();
    Mockito.verify(sender).sendMessage(ChatColor.RED + CommandAssertions.WRONG_NUM_ARGS);
    Mockito.verify(sender).sendMessage(USAGE_MESSAGE);
    Mockito.verify(sender, Mockito.times(2)).sendMessage((String) ArgumentMatchers.any());
  }

  @Test
  public void invalidImportType() {
    onCommand("oops");
    Mockito.verify(sender).sendMessage(ChatColor.RED +
        "Invalid argument 'oops'. Must be one of: [VANILLA, ADVANCED_BAN, MAX_BANS_PLUS, ESSENTIALS_X]");
    Mockito.verify(sender).sendMessage(USAGE_MESSAGE);
    Mockito.verify(sender, Mockito.times(2)).sendMessage((String) ArgumentMatchers.any());
  }


  private void onCommand(String... args) {
    new CmdImport(Mockito.mock(BukkitLogging.class), Mockito.mock(ManageMCImportPluginConfig.class))
        .execute(sender, "nobodycares", args);
  }
}
