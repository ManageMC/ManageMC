package com.managemc.spigot.command.processor.tabcompletion;

import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.spigot.command.processor.CmdBan;
import com.managemc.spigot.command.processor.tabcompletion.util.PlayerPunishmentTabCompleterTest;

public class CmdBanTabCompletionTest extends PlayerPunishmentTabCompleterTest {

  @Override
  protected CommandExecutorAsync newCommandExecutor() {
    return new CmdBan(config);
  }
}
