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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.awaitility.Awaitility.await;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CommandExecutorAsyncTest {

  @Mock
  private CommandSender sender;
  @Mock
  private Command command;
  @Mock
  private BukkitLogging logging;

  @Test
  public void success() {
    AtomicBoolean result = new AtomicBoolean();
    awaitAsyncCommand(() -> result.set(
        new ExampleExecutor(this::doNothing, this::doNothing)
            .onCommand(sender, command, "", new String[]{})
    ));

    Assert.assertTrue(result.get());
    Mockito.verify(sender, Mockito.never()).sendMessage(Mockito.anyString());
    Mockito.verify(logging, Mockito.never()).logStackTrace(Mockito.any());
  }

  @Test
  public void abortWithUsageMessage_sync() {
    AtomicBoolean result = new AtomicBoolean();
    awaitAsyncCommand(() -> result.set(
        new ExampleExecutor(() -> {
          throw AbortCommand.withUsageMessage("Ayy lmao");
        }, null).onCommand(sender, command, "", new String[]{})
    ));

    Assert.assertFalse(result.get());
    Mockito.verify(sender).sendMessage(ChatColor.RED + "Ayy lmao");
    Mockito.verify(sender, Mockito.times(1)).sendMessage(Mockito.anyString());
    Mockito.verify(logging, Mockito.never()).logStackTrace(Mockito.any());
  }

  @Test
  public void abortWithUsageMessage_async() {
    AtomicBoolean result = new AtomicBoolean();
    awaitAsyncCommand(() -> result.set(
        new ExampleExecutor(
            this::doNothing,
            () -> {
              throw AbortCommand.withUsageMessage("Ayy lmao");
            }).onCommand(sender, command, "", new String[]{})
    ));

    // we don't care about the usage message in async parts of commands
    Assert.assertTrue(result.get());
    Mockito.verify(sender).sendMessage(ChatColor.RED + "Ayy lmao");
    Mockito.verify(sender, Mockito.times(1)).sendMessage(Mockito.anyString());
    Mockito.verify(logging, Mockito.never()).logStackTrace(Mockito.any());
  }

  @Test
  public void abortWithoutUsageMessage_sync() {
    AtomicBoolean result = new AtomicBoolean();
    awaitAsyncCommand(() -> result.set(
        new ExampleExecutor(() -> {
          throw AbortCommand.withoutUsageMessage("Ayy lmao");
        }, null).onCommand(sender, command, "", new String[]{})
    ));

    Assert.assertTrue(result.get());
    Mockito.verify(sender).sendMessage(ChatColor.RED + "Ayy lmao");
    Mockito.verify(sender, Mockito.times(1)).sendMessage(Mockito.anyString());
    Mockito.verify(logging, Mockito.never()).logStackTrace(Mockito.any());
  }

  @Test
  public void abortWithoutUsageMessage_async() {
    AtomicBoolean result = new AtomicBoolean();
    awaitAsyncCommand(() -> result.set(
        new ExampleExecutor(
            this::doNothing,
            () -> {
              throw AbortCommand.withoutUsageMessage("Ayy lmao");
            }).onCommand(sender, command, "", new String[]{})
    ));

    Assert.assertTrue(result.get());
    Mockito.verify(sender).sendMessage(ChatColor.RED + "Ayy lmao");
    Mockito.verify(sender, Mockito.times(1)).sendMessage(Mockito.anyString());
    Mockito.verify(logging, Mockito.never()).logStackTrace(Mockito.any());
  }

  @Test
  public void unexpectedError_sync() {
    AtomicBoolean result = new AtomicBoolean();
    awaitAsyncCommand(() -> result.set(
        new ExampleExecutor(() -> {
          throw new RuntimeException("Ayy lmao");
        }, null).onCommand(sender, command, "", new String[]{})
    ));

    Assert.assertTrue(result.get());
    Mockito.verify(sender).sendMessage(CommandWrapping.HARD_FAILURE_MESSAGE);
    Mockito.verify(sender, Mockito.times(1)).sendMessage(Mockito.anyString());
    Mockito.verify(logging, Mockito.times(1)).logStackTrace(Mockito.any());
  }

  @Test
  public void unexpectedError_async() {
    AtomicBoolean result = new AtomicBoolean();
    awaitAsyncCommand(() -> result.set(
        new ExampleExecutor(
            this::doNothing,
            () -> {
              throw new RuntimeException("Ayy lmao");
            }).onCommand(sender, command, "", new String[]{})
    ));

    Assert.assertTrue(result.get());
    Mockito.verify(sender).sendMessage(CommandWrapping.HARD_FAILURE_MESSAGE);
    Mockito.verify(sender, Mockito.times(1)).sendMessage(Mockito.anyString());
    Mockito.verify(logging, Mockito.times(1)).logStackTrace(Mockito.any());
  }


  private void doNothing() throws InterruptedException {
    Thread.sleep(10);
  }

  protected void awaitAsyncCommand(LogicToExecute function) {
    long completedRequests = CommandExecutorAsync.COMPLETED_COMMANDS.get();
    try {
      function.apply();
    } catch (Exception e) {
      e.printStackTrace();
    }
    await().atMost(10, TimeUnit.SECONDS).until(() -> CommandExecutorAsync.COMPLETED_COMMANDS.get() > completedRequests);
  }

  private class ExampleExecutor extends CommandExecutorAsync {

    private final LogicToExecute syncLogic;
    private final LogicToExecute asyncLogic;

    public ExampleExecutor(LogicToExecute syncLogic, LogicToExecute asyncLogic) {
      super(logging);
      this.syncLogic = syncLogic;
      this.asyncLogic = asyncLogic;
    }

    @Override
    protected void preProcessCommand(CommandSender sender, String[] args) throws Exception {
      syncLogic.apply();
    }

    @Override
    protected void onCommandAsync(CommandSender sender) throws Exception {
      asyncLogic.apply();
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
