package com.managemc.spigot.command.tabcompletion;

import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.spigot.command.CmdMute;
import com.managemc.spigot.command.tabcompletion.util.PlayerPunishmentTabCompleterTest;

public class CmdMuteTabCompletionTest extends PlayerPunishmentTabCompleterTest {

  @Override
  protected CommandExecutorAsync newCommandExecutor() {
    return new CmdMute(config);
  }
}
