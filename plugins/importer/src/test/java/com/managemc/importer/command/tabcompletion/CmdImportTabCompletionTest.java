package com.managemc.importer.command.tabcompletion;

import com.managemc.importer.command.CmdImport;
import com.managemc.importer.config.ManageMCImportPluginConfig;
import com.managemc.plugins.logging.BukkitLogging;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CmdImportTabCompletionTest {

  @Mock
  private BukkitLogging logging;
  @Mock
  private ManageMCImportPluginConfig config;
  @Mock
  private CommandSender sender;
  @Mock
  private Command command;

  @Test
  public void noArgs() {
    Assert.assertEquals("[ADVANCED_BAN, ESSENTIALS_X, MAX_BANS_PLUS, VANILLA]", tabComplete());
  }

  @Test
  public void spaceOnly() {
    Assert.assertEquals("[ADVANCED_BAN, ESSENTIALS_X, MAX_BANS_PLUS, VANILLA]", tabComplete(""));
  }

  @Test
  public void match() {
    Assert.assertEquals("[VANILLA]", tabComplete("va"));
  }

  private String tabComplete(String... args) {
    List<String> result = new CmdImport(logging, config).onTabComplete(sender, command, "", args);
    return "[" + String.join(", ", result) + "]";
  }
}
