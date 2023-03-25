package com.managemc.importer.environment;

import com.managemc.api.wrapper.ClientProvider;
import com.managemc.importer.config.LocalConfig;

public class ConditionalBehavior {

  public static ClientProvider getClientProvider(ClientProvider.Logger logger, LocalConfig localConfig) {
    switch (localConfig.getEnvironment()) {
      case PRODUCTION:
        return ClientProvider.production(logger, localConfig.getKeys(), null);
      case DEMO:
        return ClientProvider.demo(logger, localConfig.getKeys(), null);
      case LOCAL:
        return ClientProvider.local(logger, localConfig.getKeys(), null);
    }

    throw new RuntimeException("Unexpected environment " + localConfig.getEnvironment());
  }
}
