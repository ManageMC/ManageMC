package com.managemc.importer.service.helper;

import com.google.common.collect.Iterables;
import com.managemc.api.ApiException;
import com.managemc.importer.data.cache.PunishmentsCache;
import com.managemc.importer.service.OnboardingApiService;
import com.managemc.plugins.logging.BukkitLogging;
import org.openapitools.client.model.ImportableBans;
import org.openapitools.client.model.ImportableIpBans;
import org.openapitools.client.model.ImportableMutes;
import org.openapitools.client.model.ImportableWarnings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class PunishmentImporter {

  private final BukkitLogging logging;
  private final OnboardingApiService onboardingApiService;
  private final PunishmentsCache punishmentsCache;
  private final int threadPoolSize;
  private final AtomicInteger successCount = new AtomicInteger();
  private final AtomicInteger failureCount = new AtomicInteger();

  public PunishmentImporter(
      BukkitLogging logging,
      OnboardingApiService onboardingApiService,
      PunishmentsCache punishmentsCache,
      int threadPoolSize
  ) {
    this.logging = logging;
    this.onboardingApiService = onboardingApiService;
    this.punishmentsCache = punishmentsCache;
    this.threadPoolSize = threadPoolSize;
  }

  public void importAllPunishments() throws Exception {
    long importId = onboardingApiService.createImport();

    ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);

    List<Callable<Object>> tasks = new ArrayList<>();

    buildPartitionedApiRequests(
        punishmentsCache.allBans(),
        (batch) -> onboardingApiService.importBans(importId, new ImportableBans().bans(batch))
    ).forEach(task -> tasks.add(Executors.callable(task)));

    buildPartitionedApiRequests(
        punishmentsCache.allMutes(),
        (batch) -> onboardingApiService.importMutes(importId, new ImportableMutes().mutes(batch))
    ).forEach(task -> tasks.add(Executors.callable(task)));

    buildPartitionedApiRequests(
        punishmentsCache.allWarnings(),
        (batch) -> onboardingApiService
            .importWarnings(importId, new ImportableWarnings().warnings(batch))
    ).forEach(task -> tasks.add(Executors.callable(task)));

    buildPartitionedApiRequests(
        punishmentsCache.allIpBans(),
        (batch) -> onboardingApiService.importIpBans(importId, new ImportableIpBans().ipBans(batch))
    ).forEach(task -> tasks.add(Executors.callable(task)));

    executorService.invokeAll(tasks);

    if (failureCount.get() == 0) {
      onboardingApiService.completeImport(importId);
    } else {
      onboardingApiService.cancelImport(importId);
    }
    executorService.shutdownNow();
  }

  public int getTotalCount() {
    return punishmentsCache.size();
  }

  public int getSuccessCount() {
    return successCount.get();
  }

  public int getFailureCount() {
    return failureCount.get();
  }

  private <T> Collection<Runnable> buildPartitionedApiRequests(List<T> allRecords, PunishmentImportApiCall<T> apiCall) {
    Collection<Runnable> runnables = new ArrayList<>();

    Iterables
        .partition(allRecords, 200)
        .forEach(batch -> runnables.add(() -> {
          try {
            if (failureCount.get() == 0) {
              apiCall.run(batch);
              successCount.addAndGet(batch.size());
            }
          } catch (ApiException e) {
            logging.logStackTrace(e);
            failureCount.addAndGet(batch.size());
          }
        }));

    return runnables;
  }

  @FunctionalInterface
  public interface PunishmentImportApiCall<T> {
    void run(List<T> batch) throws ApiException;
  }
}
