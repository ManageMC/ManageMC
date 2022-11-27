package com.managemc.importer.data;

import com.managemc.importer.service.helper.PunishmentImporter;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ImportJobTracker {

  private final AtomicInteger idIncrementer = new AtomicInteger();

  private final Map<String, PunishmentImporter> jobs = new ConcurrentHashMap<>();
  private final Map<String, String> internalIdByTempId = new ConcurrentHashMap<>();
  private final Map<String, String> tempIdByInternalId = new ConcurrentHashMap<>();

  public String trackJob(PunishmentImporter importer) {
    String nextId = String.format("t%s", idIncrementer.addAndGet(1));
    jobs.put(nextId, importer);
    return nextId;
  }

  public void updateJobId(String tempId, long internalId) {
    internalIdByTempId.put(tempId, String.valueOf(internalId));
    tempIdByInternalId.put(String.valueOf(internalId), tempId);
  }

  public Status getJobStatus(String jobId) {
    String lookupId = jobId.startsWith("t") ? jobId : tempIdByInternalId.get(jobId);
    if (lookupId == null) {
      return null;
    }

    PunishmentImporter importer = jobs.get(lookupId);
    return importer == null
        ? null
        : new Status(idToDisplay(jobId), importer);
  }

  public Map<String, Status> getAllStatuses() {
    return jobs.entrySet().stream().collect(Collectors.toMap(
        entry -> idToDisplay(entry.getKey()),
        entry -> new Status(idToDisplay(entry.getKey()), entry.getValue())
    ));
  }

  private String idToDisplay(String tempId) {
    if (internalIdByTempId.containsKey(tempId)) {
      String internalId = String.valueOf(internalIdByTempId.get(tempId));
      return String.format("%s (previously %s)", internalId, tempId);
    }

    return tempId;
  }

  public static class Status {
    @Getter
    private final String id;
    @Getter
    private final int total;
    @Getter
    private final int remaining;
    @Getter
    private final int succeeded;
    @Getter
    private final int failed;

    private Status(String id, PunishmentImporter importer) {
      this.id = id;
      this.total = importer.getTotalCount();
      this.succeeded = importer.getSuccessCount();
      this.failed = importer.getFailureCount();
      this.remaining = total - succeeded - failed;
    }
  }
}
