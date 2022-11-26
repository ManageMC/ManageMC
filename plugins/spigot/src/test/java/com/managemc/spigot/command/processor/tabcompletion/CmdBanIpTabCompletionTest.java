package com.managemc.spigot.command.processor.tabcompletion;

import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.spigot.command.processor.CmdBanIp;
import com.managemc.spigot.command.processor.tabcompletion.util.PlayerPunishmentTabCompleterTest;

public class CmdBanIpTabCompletionTest extends PlayerPunishmentTabCompleterTest {

  @Override
  protected CommandExecutorAsync newCommandExecutor() {
    return new CmdBanIp(config);
  }
}
