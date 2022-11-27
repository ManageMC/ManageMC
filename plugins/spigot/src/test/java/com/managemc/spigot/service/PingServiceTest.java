package com.managemc.spigot.service;

import com.managemc.api.ApiException;
import com.managemc.api.api.PingApi;
import com.managemc.api.wrapper.ClientProvider;
import com.managemc.api.wrapper.model.Keys;
import com.managemc.api.wrapper.refresher.ExternalServerTokenRefresher;
import com.managemc.api.wrapper.refresher.TokenRefresher;
import com.managemc.spigot.config.SpigotPluginConfigTest;
import com.managemc.spigot.testutil.TestBase;
import com.managemc.spigot.testutil.TestMocks;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

public class PingServiceTest extends TestBase {

  @Mock
  private ClientProvider.Logger logger;

  @Test
  public void whenApiKeysWrong_errorMessageAndShutdown() {
    ClientProvider badProvider = ClientProvider.demo(logger, new Keys("oops", "oops"), null);
    ApiPingService service = new SpigotPluginConfigTest(new TestMocks(), badProvider).getApiPingService();

    RuntimeException e = Assert.assertThrows(TokenRefresher.BadCredentialsException.class, service::ping);
    Assert.assertEquals(ExternalServerTokenRefresher.BAD_CREDS_MESSAGE, e.getMessage());
  }

  @Test
  public void whenUnexpectedStatus_errorMessageAndShutdown() throws ApiException {
    ApiPingService service = mockServiceWithHttpStatus(422);

    RuntimeException e = Assert.assertThrows(RuntimeException.class, service::ping);
    Assert.assertEquals(String.format(ApiPingService.UNHANDLED_ERROR_MESSAGE, 422), e.getMessage());
  }

  @Test
  public void whenServiceUnavailable_errorMessageAndShutdown() throws ApiException {
    ApiPingService service = mockServiceWithHttpStatus(502);

    RuntimeException e = Assert.assertThrows(RuntimeException.class, service::ping);
    Assert.assertEquals(ApiPingService.UNAVAILABLE_MESSAGE, e.getMessage());
  }

  @Test
  public void happyPath() {
    config.getApiPingService().ping();
  }


  private ApiPingService mockServiceWithHttpStatus(int status) throws ApiException {
    ApiPingService service = new SpigotPluginConfigTest(new TestMocks(), mockClients).getApiPingService();
    PingApi mockPingApi = Mockito.mock(PingApi.class);
    Mockito.when(mockClients.externalServer().getPingApi()).thenReturn(mockPingApi);
    Mockito.when(mockPingApi.ping()).thenThrow(new ApiException(status, "oops"));
    return service;
  }
}
