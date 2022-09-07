package com.managemc.importer;

import com.managemc.api.wrapper.ClientProvider;
import com.managemc.api.wrapper.model.Keys;
import org.mockito.Mockito;

public class TestWebClients {

  private static final String PUBLIC_KEY = "006d4fdd-8779-4e72-b699-eeaf53fd8203";
  private static final String PRIVATE_KEY = "zRaHN10wP4oGpNs5qf74Ig";

  public static final ClientProvider CLIENT_PROVIDER = ClientProvider.demo(
      Mockito.mock(ClientProvider.Logger.class),
      new Keys(PUBLIC_KEY, PRIVATE_KEY),
      null
  );
}
