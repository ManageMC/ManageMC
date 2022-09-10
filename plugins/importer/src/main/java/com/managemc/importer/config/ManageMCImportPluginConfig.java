package com.managemc.importer.config;

import com.managemc.api.wrapper.ClientProvider;
import com.managemc.importer.data.ImportJobTracker;
import com.managemc.importer.reader.source.AdvancedBanDb;
import com.managemc.importer.reader.source.AdvancedBanDbImpl;
import com.managemc.importer.reader.source.MaxBansPlusDb;
import com.managemc.importer.reader.source.MaxBansPlusDbImpl;
import com.managemc.importer.service.JobStatusService;
import com.managemc.importer.service.OnboardingApiService;
import com.managemc.importer.service.PunishmentImporterService;
import com.managemc.plugins.logging.BukkitLogging;
import lombok.Getter;

public class ManageMCImportPluginConfig {

  private final BukkitLogging logging;

  @Getter
  private final ClientProvider clientProvider;
  @Getter
  private final PunishmentImporterService importerService;
  @Getter
  private final String advancedBanMainClass = OptionalRuntimeDependencies.ADVANCED_BAN.getMainClassName();
  @Getter
  private final String maxBansMainClass = OptionalRuntimeDependencies.MAX_BANS_PLUS.getMainClassName();
  @Getter
  private final JobStatusService jobStatusService;

  public ManageMCImportPluginConfig(BukkitLogging logging, LocalConfig localConfig) {
    this.logging = logging;
    clientProvider = ClientProvider.demo(
        new ClientProviderLogger(),
        localConfig.getKeys(),
        null
    );
    OnboardingApiService onboardingApiService = new OnboardingApiService(clientProvider);
    ImportJobTracker importJobTracker = new ImportJobTracker();
    jobStatusService = new JobStatusService(importJobTracker);

    AdvancedBanDb advancedBanDb = OptionalRuntimeDependencies.ADVANCED_BAN.isAvailable()
        ? new AdvancedBanDbImpl()
        : null;
    MaxBansPlusDb maxBansPlusDb = OptionalRuntimeDependencies.MAX_BANS_PLUS.isAvailable()
        ? new MaxBansPlusDbImpl()
        : null;

    importerService = new PunishmentImporterService(
        logging,
        advancedBanDb,
        maxBansPlusDb,
        onboardingApiService,
        importJobTracker
    );
  }

  private class ClientProviderLogger implements ClientProvider.Logger {

    @Override
    public void logWarning(String s) {
      logging.logWarning(s);
    }

    @Override
    public void logInfo(String s) {
      logging.logInfo(s);
    }
  }
}
