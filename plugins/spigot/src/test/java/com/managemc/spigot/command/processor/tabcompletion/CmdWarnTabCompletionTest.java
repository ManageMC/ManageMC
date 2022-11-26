package com.managemc.spigot.command.processor.tabcompletion;

import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.spigot.command.processor.CmdWarn;
import com.managemc.spigot.command.processor.tabcompletion.util.OnlinePlayersTabCompleterTest;

public class CmdWarnTabCompletionTest extends OnlinePlayersTabCompleterTest {

  @Override
  protected CommandExecutorAsync newCommandExecutor() {
    return new CmdWarn(config);
  }
}
