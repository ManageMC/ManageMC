package com.managemc.linker.service;

import com.managemc.api.ApiException;
import com.managemc.api.wrapper.ClientProvider;
import com.managemc.api.wrapper.model.Keys;
import com.managemc.api.wrapper.refresher.TokenRefresher;
import com.managemc.linker.config.AccountLinkerConfig;
import com.managemc.linker.testutil.AccountLinkerConfigTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ApiPingServiceTest {

  private static final AccountLinkerConfig CONFIG = new AccountLinkerConfigTest();

  private ApiPingService validator;

  @Before
  public void setup() {
    validator = CONFIG.getApiPingService();
  }

  @Test
  public void happyPath() throws ApiException {
    validator.ping();
  }

  @Test(expected = TokenRefresher.BadCredentialsException.class)
  public void badCredentials() throws ApiException {
    ClientProvider clientProvider = ClientProvider.local(
        Mockito.mock(ClientProvider.Logger.class),
        new Keys(AccountLinkerConfigTest.PUBLIC_KEY, "oops"),
        null
    );

    validator = new ApiPingService(clientProvider);
    validator.ping();
  }
}
