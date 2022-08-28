package com.managemc.linker.config;

import com.managemc.linker.service.AccountLinkingService;
import com.managemc.linker.service.ApiPingService;

public interface AccountLinkerConfig {

  ApiPingService getApiPingService();

  AccountLinkingService getAccountLinkingService();
}
