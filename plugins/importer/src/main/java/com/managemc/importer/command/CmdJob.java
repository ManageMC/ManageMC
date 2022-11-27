package com.managemc.importer.command;

import com.managemc.importer.command.util.CommandAssertions;
import com.managemc.importer.service.JobStatusService;
import com.managemc.plugins.command.CommandExecutorSync;
import com.managemc.plugins.logging.BukkitLogging;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CmdJob extends CommandExecutorSync {

  private final JobStatusService jobStatusService;

  public CmdJob(BukkitLogging logging, JobStatusService jobStatusService) {
    super(logging);
    this.jobStatusService = jobStatusService;
  }

  @Override
  protected void onCommand(CommandSender sender, String[] args) {
    CommandAssertions.assertConsoleSender(sender);
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
    if (sender instanceof ConsoleCommandSender) {
      switch (args.length) {
        case 0:
          return matchingActions("");
        case 1:
          return matchingActions(args[0]);
      }
    }

    return null;
  }

  private List<String> matchingActions(String lastArg) {
    String lastArgLower = lastArg.toLowerCase();

    return Arrays.stream(Action.values())
        .map(Object::toString)
        .filter(v -> v.toLowerCase().startsWith(lastArgLower))
        .sorted()
        .collect(Collectors.toList());
  }

  private enum Action {
    LIST, STATUS
  }
}
