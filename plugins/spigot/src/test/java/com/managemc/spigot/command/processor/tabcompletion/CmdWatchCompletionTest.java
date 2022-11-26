package com.managemc.spigot.command.processor.tabcompletion;

import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.spigot.command.processor.CmdWatch;
import com.managemc.spigot.command.processor.tabcompletion.util.OnlinePlayersTabCompleterTest;

public class CmdWatchCompletionTest extends OnlinePlayersTabCompleterTest {

  @Override
  protected CommandExecutorAsync newCommandExecutor() {
    return new CmdWatch(config);
  }
}
