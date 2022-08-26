package com.managemc.account_linker.config;

import com.managemc.api.wrapper.ClientProvider;
import com.managemc.plugins.logging.BukkitLogging;
import com.managemc.account_linker.model.LocalConfig;
import com.managemc.account_linker.service.AccountLinkingService;
import com.managemc.account_linker.service.ApiPingService;
import lombok.Getter;

public class AccountLinkerConfigLocal implements AccountLinkerConfig {

  private final BukkitLogging logger;

  @Getter
  private final ApiPingService apiPingService;
  @Getter
  private final AccountLinkingService accountLinkingService;

  public AccountLinkerConfigLocal(BukkitLogging logger, LocalConfig localConfig) {
    this.logger = logger;

    ClientProvider clientProvider = ClientProvider.local(new ClientProviderLogger(), localConfig.getKeys(), null);
    this.apiPingService = new ApiPingService(clientProvider);
    this.accountLinkingService = new AccountLinkingService(logger, clientProvider);
  }

  private class ClientProviderLogger implements ClientProvider.Logger {

    @Override
    public void logWarning(String s) {
      logger.logWarning(s);
    }

    @Override
    public void logInfo(String s) {
      logger.logInfo(s);
    }
  }
}
