package com.managemc.importer.command.tabcompletion;

import com.managemc.importer.command.CmdJob;
import com.managemc.importer.service.JobStatusService;
import com.managemc.plugins.logging.BukkitLogging;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CmdJobTabCompletionTest {

  @Mock
  private BukkitLogging logging;
  @Mock
  private JobStatusService service;
  @Mock
  private CommandSender sender;
  @Mock
  private Command command;

  @Test
  public void noArgs() {
    Assert.assertEquals("[LIST, STATUS]", tabComplete());
  }

  @Test
  public void spaceOnly() {
    Assert.assertEquals("[LIST, STATUS]", tabComplete(""));
  }

  @Test
  public void match() {
    Assert.assertEquals("[STATUS]", tabComplete("s"));
  }

  private String tabComplete(String... args) {
    List<String> result = new CmdJob(logging, service).onTabComplete(sender, command, "", args);
    return "[" + String.join(", ", result) + "]";
  }
}
