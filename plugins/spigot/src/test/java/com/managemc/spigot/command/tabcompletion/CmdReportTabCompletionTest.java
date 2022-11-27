package com.managemc.spigot.command.tabcompletion;

import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.spigot.command.CmdReport;
import com.managemc.spigot.command.tabcompletion.util.OnlinePlayersTabCompleterTest;

public class CmdReportTabCompletionTest extends OnlinePlayersTabCompleterTest {

  @Override
  protected CommandExecutorAsync newCommandExecutor() {
    return new CmdReport(config);
  }
}
