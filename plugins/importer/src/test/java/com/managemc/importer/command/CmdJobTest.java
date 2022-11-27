package com.managemc.importer.command;

import com.managemc.importer.command.util.CommandAssertions;
import com.managemc.importer.data.ImportJobTracker;
import com.managemc.importer.service.JobStatusService;
import com.managemc.importer.service.helper.PunishmentImporter;
import com.managemc.plugins.logging.BukkitLogging;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CmdJobTest {

  @Mock
  private CommandSender sender;
  @Mock
  private Command command;

  private ImportJobTracker importJobTracker;
  private JobStatusService jobStatusService;

  @Before
  public void setup() {
    importJobTracker = new ImportJobTracker();
    jobStatusService = new JobStatusService(importJobTracker);
  }

  @Test
  public void noArgs() {
    boolean result = onCommand();

    Assert.assertFalse(result);
    verify(sender).sendMessage(ChatColor.RED + CommandAssertions.WRONG_NUM_ARGS);
    verify(sender, times(1)).sendMessage((String) any());
  }

  @Test
  public void invalidAction() {
    boolean result = onCommand("covfefe");

    Assert.assertFalse(result);
    verify(sender).sendMessage(ChatColor.RED + "Invalid argument 'covfefe'. Must be one of: [LIST, STATUS]");
    verify(sender, times(1)).sendMessage((String) any());
  }

  @Test
  public void list_withNoJobs() {
    boolean result = onCommand("list");

    Assert.assertTrue(result);
    verify(sender).sendMessage(ChatColor.BLUE + "No job history yet");
    verify(sender, times(1)).sendMessage((String) any());
  }

  @Test
  public void list_withJobs() {
    buildAndTrackJob(10, 0, 10);
    buildAndTrackJob(9, 1, 20);
    buildAndTrackJob(5, 0, 10);
    boolean result = onCommand("list");

    Assert.assertTrue(result);
    verify(sender).sendMessage(ArgumentMatchers.contains("imported 10 punishment(s) successfully"));
    verify(sender).sendMessage(ArgumentMatchers.contains("see server logs"));
    verify(sender).sendMessage(ArgumentMatchers.contains("imported 5/10 punishment(s) so far"));
    verify(sender, times(3)).sendMessage((String) any());
  }

  @Test
  public void status_noSecondArgument() {
    boolean result = onCommand("status");

    Assert.assertFalse(result);
    verify(sender).sendMessage(ChatColor.RED + CommandAssertions.WRONG_NUM_ARGS);
    verify(sender, times(1)).sendMessage((String) any());
  }

  @Test
  public void status_tooManyArgs() {
    boolean result = onCommand("status", "1", "oops");

    Assert.assertFalse(result);
    verify(sender).sendMessage(ChatColor.RED + CommandAssertions.WRONG_NUM_ARGS);
    verify(sender, times(1)).sendMessage((String) any());
  }

  @Test
  public void status_secondArgumentNotANumber() {
    boolean result = onCommand("status", "oops");

    Assert.assertFalse(result);
    verify(sender).sendMessage(ChatColor.RED + "Invalid integer oops");
    verify(sender, times(1)).sendMessage((String) any());
  }

  @Test
  public void status_jobNotFound() {
    boolean result = onCommand("status", "0");

    Assert.assertTrue(result);
    verify(sender).sendMessage(ChatColor.RED + "No job with ID 0");
    verify(sender, times(1)).sendMessage((String) any());
  }

  @Test
  public void status_successfulJob() {
    buildAndTrackJob(1, 0, 1);
    boolean result = onCommand("status", "1");

    Assert.assertTrue(result);
    verify(sender).sendMessage(ArgumentMatchers.contains("imported 1 punishment(s) successfully"));
    verify(sender, times(1)).sendMessage((String) any());
  }

  @Test
  public void status_inProgressJob() {
    buildAndTrackJob(5, 0, 7);
    boolean result = onCommand("status", "1");

    Assert.assertTrue(result);
    verify(sender).sendMessage(ArgumentMatchers.contains("imported 5/7 punishment(s) so far"));
    verify(sender, times(1)).sendMessage((String) any());
  }

  @Test
  public void status_failedJob() {
    buildAndTrackJob(5, 1, 7);
    boolean result = onCommand("status", "1");

    Assert.assertTrue(result);
    verify(sender).sendMessage(ArgumentMatchers.contains("see server logs"));
    verify(sender, times(1)).sendMessage((String) any());
  }


  private void buildAndTrackJob(int successCount, int failedCount, int totalCount) {
    PunishmentImporter importer = Mockito.mock(PunishmentImporter.class);
    Mockito.when(importer.getTotalCount()).thenReturn(totalCount);
    Mockito.when(importer.getSuccessCount()).thenReturn(successCount);
    Mockito.when(importer.getFailureCount()).thenReturn(failedCount);
    importJobTracker.trackJob(importer);
  }

  private boolean onCommand(String... args) {
    return new CmdJob(Mockito.mock(BukkitLogging.class), jobStatusService).onCommand(sender, command, "", args);
  }
}
