package com.managemc.importer.command;

import com.managemc.importer.command.base.CommandAssertions;
import com.managemc.importer.command.base.CommandBaseSync;
import com.managemc.importer.command.base.CommandBuilder;
import com.managemc.importer.service.JobStatusService;
import com.managemc.plugins.logging.BukkitLogging;
import org.bukkit.command.CommandSender;

public class CmdJob extends CommandBaseSync {

  static final String USAGE = "/job <action (one of LIST, STATUS)> [job id for STATUS only]";
  static final String DESCRIPTION = "List all punishment import jobs or get the status of a job.";

  private static final CommandBuilder COMMAND_BUILDER = CommandBuilder.builder()
      .description(DESCRIPTION)
      .name("job")
      .usage(USAGE)
      .build();

  private final JobStatusService jobStatusService;

  public CmdJob(BukkitLogging logging, JobStatusService jobStatusService) {
    super(COMMAND_BUILDER, logging);
    this.jobStatusService = jobStatusService;
  }

  @Override
  public void onCommand(CommandSender sender, String[] args) {
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

  private enum Action {
    LIST, STATUS
  }
}
