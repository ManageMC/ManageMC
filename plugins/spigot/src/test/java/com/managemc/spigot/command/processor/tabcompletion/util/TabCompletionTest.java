package com.managemc.spigot.command.processor.tabcompletion.util;

import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.spigot.testutil.TestBase;

import java.util.List;

public abstract class TabCompletionTest extends TestBase {

  protected String tabComplete(String... args) {
    List<String> completions = newCommandExecutor().onTabComplete(sender, command, "", args);
    return "[" + String.join(", ", completions) + "]";
  }

  protected abstract CommandExecutorAsync newCommandExecutor();
}
