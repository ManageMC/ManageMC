package com.managemc.importer.data;

import com.managemc.importer.service.helper.PunishmentImporter;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ImportJobTracker {

  private final AtomicInteger idIncrementer = new AtomicInteger();

  private final Map<Integer, PunishmentImporter> jobs = new ConcurrentHashMap<>();

  public int trackJob(PunishmentImporter importer) {
    int nextId = idIncrementer.addAndGet(1);
    jobs.put(nextId, importer);
    return nextId;
  }

  public Status getJobStatus(int jobId) {
    PunishmentImporter importer = jobs.get(jobId);
    return importer == null
        ? null
        : new Status(importer);
  }

  public Map<Integer, Status> getAllStatuses() {
    return jobs.entrySet().stream().collect(Collectors.toMap(
        Map.Entry::getKey,
        entry -> new Status(entry.getValue())
    ));
  }

  public static class Status {
    @Getter
    private final int total;
    @Getter
    private final int remaining;
    @Getter
    private final int succeeded;
    @Getter
    private final int failed;

    private Status(PunishmentImporter importer) {
      this.total = importer.getTotalCount();
      this.succeeded = importer.getSuccessCount();
      this.failed = importer.getFailureCount();
      this.remaining = total - succeeded - failed;
    }
  }
}
