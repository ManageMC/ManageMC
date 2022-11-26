package com.managemc.spigot.command.processor.tabcompletion;

import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.spigot.command.processor.CmdMute;
import com.managemc.spigot.command.processor.tabcompletion.util.PlayerPunishmentTabCompleterTest;

public class CmdMuteTabCompletionTest extends PlayerPunishmentTabCompleterTest {

  @Override
  protected CommandExecutorAsync newCommandExecutor() {
    return new CmdMute(config);
  }
}
