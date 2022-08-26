package com.managemc.account_linker.config;

import com.managemc.account_linker.service.AccountLinkingService;
import com.managemc.account_linker.service.ApiPingService;

public interface AccountLinkerConfig {

  ApiPingService getApiPingService();

  AccountLinkingService getAccountLinkingService();
}
