package com.managemc.spigot.command.processor.tabcompletion;

import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.spigot.command.processor.CmdUnwatch;
import com.managemc.spigot.command.processor.tabcompletion.util.OnlinePlayersTabCompleterTest;

public class CmdUnwatchCompletionTest extends OnlinePlayersTabCompleterTest {

  @Override
  protected CommandExecutorAsync newCommandExecutor() {
    return new CmdUnwatch(config);
  }
}
