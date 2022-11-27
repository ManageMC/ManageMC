package com.managemc.spigot.testutil;

import com.managemc.api.wrapper.ClientProvider;
import com.managemc.api.wrapper.model.Keys;
import org.mockito.Mockito;

public class TestWebClients {

  public static final ClientProvider CLIENT_PROVIDER = ClientProvider.demo(
      Mockito.mock(ClientProvider.Logger.class),
      new Keys(TestConstants.PUBLIC_KEY, TestConstants.PRIVATE_KEY),
      TestConstants.KITPVP_LABEL
  );
}
