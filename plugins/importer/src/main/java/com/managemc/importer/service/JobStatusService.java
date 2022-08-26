package com.managemc.importer.service;

import com.managemc.importer.data.ImportJobTracker;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Map;

public class JobStatusService {

  private final ImportJobTracker jobTracker;

  public JobStatusService(ImportJobTracker jobTracker) {
    this.jobTracker = jobTracker;
  }

  public void displayAllJobStatuses(CommandSender sender) {
    Map<Integer, ImportJobTracker.Status> allStatuses = jobTracker.getAllStatuses();
    if (allStatuses.isEmpty()) {
      sender.sendMessage(ChatColor.BLUE + "No job history yet");
    } else {
    allStatuses.forEach((jobId, status) -> displayJobStatus(sender, jobId, status));
    }
  }

  public void displayJobStatus(CommandSender sender, int jobId) {
    ImportJobTracker.Status status = jobTracker.getJobStatus(jobId);
    if (status == null) {
      sender.sendMessage(ChatColor.RED + "No job with ID " + jobId);
    } else {
      displayJobStatus(sender, jobId, status);
    }
  }

  private void displayJobStatus(CommandSender sender, int jobId, ImportJobTracker.Status status) {
    if (status.getFailed() > 0) {
      String message = String.format(
          "%s%s: %sFAILED%s - see server logs",
          ChatColor.BLUE,
          jobId,
          ChatColor.RED,
          ChatColor.BLUE
      );
      sender.sendMessage(message);
      return;
    }

    if (status.getRemaining() == 0) {
      String message = String.format(
          "%s%s: %sDONE%s - imported %s punishment(s) successfully",
          ChatColor.BLUE,
          jobId,
          ChatColor.GREEN,
          ChatColor.BLUE,
          status.getTotal()
      );
      sender.sendMessage(message);
      return;
    }

    String message = String.format(
        "%s%s: %sIN PROGRESS%s - imported %s/%s punishment(s) so far",
        ChatColor.BLUE,
        jobId,
        ChatColor.YELLOW,
        ChatColor.BLUE,
        status.getSucceeded(),
        status.getTotal()
    );
    sender.sendMessage(message);
  }
}
