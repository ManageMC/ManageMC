package com.managemc.spigot.command.tabcompletion;

import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.spigot.command.CmdBan;
import com.managemc.spigot.command.tabcompletion.util.PlayerPunishmentTabCompleterTest;

public class CmdBanTabCompletionTest extends PlayerPunishmentTabCompleterTest {

  @Override
  protected CommandExecutorAsync newCommandExecutor() {
    return new CmdBan(config);
  }
}
