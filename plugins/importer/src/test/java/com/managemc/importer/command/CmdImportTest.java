package com.managemc.importer.command;

import com.managemc.importer.command.util.CommandAssertions;
import com.managemc.importer.config.ManageMCImportPluginConfig;
import com.managemc.plugins.logging.BukkitLogging;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CmdImportTest {

  private CommandSender sender;
  @Mock
  private Command command;

  @Before
  public void setup() {
    sender = Mockito.mock(ConsoleCommandSender.class);
  }

  @Test
  public void noArgs() {
    boolean result = onCommand();

    Assert.assertFalse(result);
    Mockito.verify(sender).sendMessage(ChatColor.RED + CommandAssertions.WRONG_NUM_ARGS);
    Mockito.verify(sender, Mockito.times(1)).sendMessage((String) ArgumentMatchers.any());
  }

  @Test
  public void invalidImportType() {
    boolean result = onCommand("oops");

    Assert.assertFalse(result);
    Mockito.verify(sender).sendMessage(ChatColor.RED +
        "Invalid argument 'oops'. Must be one of: [VANILLA, ADVANCED_BAN, MAX_BANS_PLUS, ESSENTIALS_X]");
    Mockito.verify(sender, Mockito.times(1)).sendMessage((String) ArgumentMatchers.any());
  }

  @Test
  public void playerSender() {
    sender = Mockito.mock(Player.class);
    Assert.assertTrue(onCommand());

    Mockito.verify(sender).sendMessage(ChatColor.RED + CommandAssertions.CONSOLE_SENDER_ONLY);
    Mockito.verify(sender, Mockito.times(1)).sendMessage(Mockito.anyString());
  }


  private boolean onCommand(String... args) {
    return new CmdImport(Mockito.mock(BukkitLogging.class), Mockito.mock(ManageMCImportPluginConfig.class))
        .onCommand(sender, command, "", args);
  }
}
