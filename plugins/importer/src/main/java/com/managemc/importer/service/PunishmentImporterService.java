package com.managemc.importer.service;

import com.managemc.importer.data.ImportJobTracker;
import com.managemc.importer.data.cache.*;
import com.managemc.importer.reader.*;
import com.managemc.importer.reader.source.AdvancedBanDb;
import com.managemc.importer.reader.source.MaxBansPlusDb;
import com.managemc.importer.service.helper.PunishmentImporter;
import com.managemc.plugins.logging.BukkitLogging;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.File;

public class PunishmentImporterService {

  private static final int DEFAULT_THREAD_POOL_SIZE = 10;

  public static final String READING_MESSAGE = String.format(
      "%sReading punishments from data source...",
      ChatColor.BLUE
  );
  public static final String IMPORTING_MESSAGE = String.format(
      "%sImporting punishments (this may take a while)...",
      ChatColor.BLUE
  );
  public static final String NO_PUNISHMENTS_FOUND_MESSAGE = String.format(
      "%sNo punishments found",
      ChatColor.YELLOW
  );
  public static final String FINISHED_MESSAGE = String.format(
      "%sImport complete.",
      ChatColor.GREEN
  );
  public static final String IMPORT_FAILED_MESSAGE = String.format(
      "%s Failed to import punishments",
      ChatColor.RED
  );

  private final BukkitLogging logging;
  private final AdvancedBanDb advancedBanDb;
  private final MaxBansPlusDb maxBansDb;
  private final OnboardingApiService onboardingApiService;
  private final ImportJobTracker jobTracker;
  private final int threadPoolSize;

  public PunishmentImporterService(
      BukkitLogging logging,
      AdvancedBanDb advancedBanDb,
      MaxBansPlusDb maxBansDb,
      OnboardingApiService onboardingApiService,
      ImportJobTracker jobTracker
  ) {
    this(logging, advancedBanDb, maxBansDb, onboardingApiService, jobTracker, DEFAULT_THREAD_POOL_SIZE);
  }

  public PunishmentImporterService(
      BukkitLogging logging,
      AdvancedBanDb advancedBanDb,
      MaxBansPlusDb maxBansDb,
      OnboardingApiService onboardingApiService,
      ImportJobTracker jobTracker,
      int threadPoolSize
  ) {
    this.logging = logging;
    this.advancedBanDb = advancedBanDb;
    this.maxBansDb = maxBansDb;
    this.onboardingApiService = onboardingApiService;
    this.jobTracker = jobTracker;
    this.threadPoolSize = threadPoolSize;
  }

  public void importVanilla(CommandSender sender) {
    VanillaPunishmentsCache punishmentsCache = new VanillaPunishmentsCache();
    PunishmentImporter importer = newPunishmentImporter(punishmentsCache.getBaseCache());
    VanillaBanReader banReader = new VanillaBanReader();
    VanillaIpBanReader ipBanReader = new VanillaIpBanReader();

    runImportTask(
        sender,
        punishmentsCache.getBaseCache(),
        importer,
        () -> {
          banReader.read().forEach(punishmentsCache::add);
          ipBanReader.read().forEach(punishmentsCache::add);
        });
  }

  public void importAdvancedBan(CommandSender sender) {
    AdvancedBanPunishmentsCache punishmentsCache = new AdvancedBanPunishmentsCache();
    PunishmentImporter importer = newPunishmentImporter(punishmentsCache.getBaseCache());
    AdvancedBanReader reader = new AdvancedBanReader(advancedBanDb);

    runImportTask(
        sender,
        punishmentsCache.getBaseCache(),
        importer,
        () -> reader.read().forEach(punishmentsCache::add)
    );
  }

  public void importMaxBans(CommandSender sender) {
    MaxBansPunishmentCache punishmentsCache = new MaxBansPunishmentCache();
    PunishmentImporter importer = newPunishmentImporter(punishmentsCache.getBaseCache());
    MaxBansPlusReader reader = new MaxBansPlusReader(maxBansDb);
    runImportTask(
        sender,
        punishmentsCache.getBaseCache(),
        importer,
        () -> reader.read().forEach(punishmentsCache::add)
    );
  }

  public void importEssentialsX(CommandSender sender, File dataFolder) {
    EssentialsPunishmentsCache punishmentsCache = new EssentialsPunishmentsCache();
    PunishmentImporter importer = newPunishmentImporter(punishmentsCache.getBaseCache());
    VanillaBanReader banReader = new VanillaBanReader();
    VanillaIpBanReader ipBanReader = new VanillaIpBanReader();
    EssentialsReader essentialsReader = new EssentialsReader(dataFolder);

    runImportTask(
        sender,
        punishmentsCache.getBaseCache(),
        importer,
        () -> {
          banReader.read().forEach(punishmentsCache::add);
          ipBanReader.read().forEach(punishmentsCache::add);
          essentialsReader.read().forEach(punishmentsCache::add);
        });
  }

  private PunishmentImporter newPunishmentImporter(PunishmentsCache punishmentsCache) {
    return new PunishmentImporter(logging, onboardingApiService, punishmentsCache, threadPoolSize);
  }

  @FunctionalInterface
  public interface RunnableThrowingException {
    void run() throws Exception;
  }

  private void runImportTask(
      CommandSender sender,
      PunishmentsCache punishmentsCache,
      PunishmentImporter importer,
      RunnableThrowingException readTask
  ) {
    try {
      String jobId = jobTracker.trackJob(importer);
      String beganMessage = String.format(
          "%sPunishment import job has begun with temporary ID %s%s%s. To check its status, use the /job command.",
          ChatColor.BLUE,
          ChatColor.GOLD,
          jobId,
          ChatColor.BLUE
      );
      sender.sendMessage(String.format(beganMessage, jobId));
      sender.sendMessage(READING_MESSAGE);
      readTask.run();
      if (punishmentsCache.size() > 0) {
        sender.sendMessage(IMPORTING_MESSAGE);
        long jobInternalId = importer.importAllPunishments();
        jobTracker.updateJobId(jobId, jobInternalId);
        String message = importer.getFailureCount() == 0
            ? FINISHED_MESSAGE
            : IMPORT_FAILED_MESSAGE;
        sender.sendMessage(message);
      } else {
        sender.sendMessage(NO_PUNISHMENTS_FOUND_MESSAGE);
      }
    } catch (Exception e) {
      logging.logStackTrace(e);
      sender.sendMessage(IMPORT_FAILED_MESSAGE);
    }
  }
}
