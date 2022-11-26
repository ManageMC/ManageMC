package com.managemc.spigot.command.processor.tabcompletion;

import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.spigot.command.processor.CmdReport;
import com.managemc.spigot.command.processor.tabcompletion.util.OnlinePlayersTabCompleterTest;

public class CmdReportTabCompletionTest extends OnlinePlayersTabCompleterTest {

  @Override
  protected CommandExecutorAsync newCommandExecutor() {
    return new CmdReport(config);
  }
}
