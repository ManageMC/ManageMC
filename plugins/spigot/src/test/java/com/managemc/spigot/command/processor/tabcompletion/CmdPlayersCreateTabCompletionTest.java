package com.managemc.spigot.command.processor.tabcompletion;

import com.managemc.spigot.testutil.TestBase;
import org.junit.Assert;
import org.junit.Test;

public class CmdPlayersCreateTabCompletionTest extends TestBase {

  @Test
  public void tabCompletion() {
    stubOnlinePlayers("asd");

    Assert.assertEquals("[]", onTabComplete("players", "create", "as"));
  }
}
