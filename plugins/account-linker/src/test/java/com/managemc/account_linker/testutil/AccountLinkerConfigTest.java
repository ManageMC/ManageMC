package com.managemc.account_linker.testutil;

import com.managemc.account_linker.config.AccountLinkerConfig;
import com.managemc.api.wrapper.ClientProvider;
import com.managemc.api.wrapper.model.Keys;
import com.managemc.plugins.logging.BukkitLogging;
import com.managemc.account_linker.service.AccountLinkingService;
import com.managemc.account_linker.service.ApiPingService;
import org.mockito.Mockito;

public class AccountLinkerConfigTest implements AccountLinkerConfig {

  public static final String PUBLIC_KEY = "afbd5735-de32-47a1-b6a1-264f5fb57565";
  public static final String PRIVATE_KEY = "wjuNCHscWLMzQYoU_4y37g";
  private static final ClientProvider CLIENT_PROVIDER;

  static {
    CLIENT_PROVIDER = ClientProvider
        .local(Mockito.mock(ClientProvider.Logger.class), new Keys(PUBLIC_KEY, PRIVATE_KEY), null);
  }

  @Override
  public ApiPingService getApiPingService() {
    return new ApiPingService(CLIENT_PROVIDER);
  }

  @Override
  public AccountLinkingService getAccountLinkingService() {
    return new AccountLinkingService(Mockito.mock(BukkitLogging.class), CLIENT_PROVIDER);
  }
}
