package com.managemc.plugins.command;

import com.managemc.plugins.logging.BukkitLogging;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CommandExecutorSyncTest {

  @Mock
  private CommandSender sender;
  @Mock
  private Command command;
  @Mock
  private BukkitLogging logging;

  @Test
  public void success() {
    boolean result = new ExampleExecutor(this::doNothing)
        .onCommand(sender, command, "", new String[]{});

    Assert.assertTrue(result);
    Mockito.verify(sender, Mockito.never()).sendMessage(Mockito.anyString());
    Mockito.verify(logging, Mockito.never()).logStackTrace(Mockito.any());
  }

  @Test
  public void abortWithUsageMessage() {
    boolean result = new ExampleExecutor(() -> {
      throw AbortCommand.withUsageMessage("Ayy lmao");
    }).onCommand(sender, command, "", new String[]{});

    Assert.assertFalse(result);
    Mockito.verify(sender).sendMessage(ChatColor.RED + "Ayy lmao");
    Mockito.verify(sender, Mockito.times(1)).sendMessage(Mockito.anyString());
    Mockito.verify(logging, Mockito.never()).logStackTrace(Mockito.any());
  }

  @Test
  public void abortWithoutUsageMessage() {
    boolean result = new ExampleExecutor(() -> {
      throw AbortCommand.withoutUsageMessage("Ayy lmao");
    }).onCommand(sender, command, "", new String[]{});

    Assert.assertTrue(result);
    Mockito.verify(sender).sendMessage(ChatColor.RED + "Ayy lmao");
    Mockito.verify(sender, Mockito.times(1)).sendMessage(Mockito.anyString());
    Mockito.verify(logging, Mockito.never()).logStackTrace(Mockito.any());
  }

  @Test
  public void unexpectedError() {
    boolean result = new ExampleExecutor(() -> {
      throw new RuntimeException("oops");
    }).onCommand(sender, command, "", new String[]{});

    Assert.assertTrue(result);
    Mockito.verify(sender).sendMessage(CommandWrapping.HARD_FAILURE_MESSAGE);
    Mockito.verify(sender, Mockito.times(1)).sendMessage(Mockito.anyString());
    Mockito.verify(logging, Mockito.times(1)).logStackTrace(Mockito.any());
  }


  private void doNothing() throws InterruptedException {
    Thread.sleep(10);
  }

  private class ExampleExecutor extends CommandExecutorSync {

    private final LogicToExecute logic;

    public ExampleExecutor(LogicToExecute logic) {
      super(logging);
      this.logic = logic;
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) throws Exception {
      logic.apply();
    }

    @Override
    protected List<String> onTabComplete(CommandSender sender, String[] args) {
      return null;
    }
  }

  @FunctionalInterface
  private interface LogicToExecute {
    void apply() throws Exception;
  }
}
