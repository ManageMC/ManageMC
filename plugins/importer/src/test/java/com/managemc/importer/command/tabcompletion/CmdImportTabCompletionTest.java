package com.managemc.importer.command.tabcompletion;

import com.managemc.importer.command.CmdImport;
import com.managemc.importer.config.ManageMCImportPluginConfig;
import com.managemc.plugins.logging.BukkitLogging;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CmdImportTabCompletionTest {

  @Mock
  private BukkitLogging logging;
  @Mock
  private ManageMCImportPluginConfig config;
  @Mock
  private Command command;

  private CommandSender sender;

  @Before
  public void setup() {
    sender = Mockito.mock(ConsoleCommandSender.class);
  }

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

  @Test
  public void playerSender() {
    sender = Mockito.mock(Player.class);

    Assert.assertEquals("[]", tabComplete("va"));
  }

  private String tabComplete(String... args) {
    List<String> result = new CmdImport(logging, config).onTabComplete(sender, command, "", args);
    return "[" + String.join(", ", result) + "]";
  }
}
