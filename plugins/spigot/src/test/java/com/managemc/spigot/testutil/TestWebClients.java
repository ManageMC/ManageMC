package com.managemc.spigot.testutil;

import com.managemc.api.wrapper.ClientProvider;
import com.managemc.api.wrapper.client.ExternalServerClient;
import com.managemc.api.wrapper.client.InternalClient;
import com.managemc.api.wrapper.client.PlayerClient;
import com.managemc.api.wrapper.model.Keys;
import org.mockito.Mockito;

public class TestWebClients {

  public static final ClientProvider CLIENT_PROVIDER = ClientProvider.demo(
      Mockito.mock(ClientProvider.Logger.class),
      new Keys(TestConstants.PUBLIC_KEY, TestConstants.PRIVATE_KEY),
      TestConstants.KITPVP_LABEL
  );

  @Deprecated // use TestBase#mockClients instead
  public static ClientProvider mockClients() {
    InternalClient internalClient = Mockito.mock(InternalClient.class);
    ExternalServerClient externalClient = Mockito.mock(ExternalServerClient.class);
    PlayerClient playerClient = Mockito.mock(PlayerClient.class);

    ClientProvider mockClient = Mockito.mock(ClientProvider.class);
    Mockito.when(mockClient.internal()).thenReturn(internalClient);
    Mockito.when(mockClient.externalServer()).thenReturn(externalClient);
    Mockito.when(mockClient.player(Mockito.any())).thenReturn(playerClient);

    return mockClient;
  }
}
