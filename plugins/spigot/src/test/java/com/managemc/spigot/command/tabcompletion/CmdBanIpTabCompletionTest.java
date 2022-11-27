package com.managemc.spigot.command.tabcompletion;

import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.spigot.command.CmdBanIp;
import com.managemc.spigot.command.tabcompletion.util.PlayerPunishmentTabCompleterTest;

public class CmdBanIpTabCompletionTest extends PlayerPunishmentTabCompleterTest {

  @Override
  protected CommandExecutorAsync newCommandExecutor() {
    return new CmdBanIp(config);
  }
}
