package com.managemc.importer.command;

import com.managemc.importer.command.util.CommandAssertions;
import com.managemc.importer.service.JobStatusService;
import com.managemc.plugins.command.CommandExecutorSync;
import com.managemc.plugins.logging.BukkitLogging;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CmdJob extends CommandExecutorSync {

  private final JobStatusService jobStatusService;

  public CmdJob(BukkitLogging logging, JobStatusService jobStatusService) {
    super(logging);
    this.jobStatusService = jobStatusService;
  }

  @Override
  protected void onCommand(CommandSender sender, String[] args) {
    CommandAssertions.assertArgsLengthAtLeast(args, 1);
    Action action = CommandAssertions.assertOneOf(args[0], Action.class);

    switch (action) {
      case LIST:
        CommandAssertions.assertArgsLength(args, 1);
        jobStatusService.displayAllJobStatuses(sender);
        break;
      case STATUS:
        CommandAssertions.assertArgsLength(args, 2);
        int jobId = CommandAssertions.assertInteger(args[1]);
        jobStatusService.displayJobStatus(sender, jobId);
        break;
      default:
        throw new RuntimeException("Unexpected action " + action);
    }
  }

  @Override
  protected List<String> onTabComplete(CommandSender sender, String[] args) {
    // TODO
    return null;
  }

  private enum Action {
    LIST, STATUS
  }
}
