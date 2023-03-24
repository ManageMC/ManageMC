package com.managemc.spigot.environment;

import com.managemc.api.wrapper.ClientProvider;
import com.managemc.spigot.config.model.LocalConfig;

public class ConditionalBehavior {

  public static ClientProvider getClientProvider(ClientProvider.Logger logger, LocalConfig localConfig) {
    switch (localConfig.getEnvironment()) {
      case PRODUCTION:
        return ClientProvider.production(logger, localConfig.getKeys(), localConfig.getServerGroup());
      case DEMO:
        return ClientProvider.demo(logger, localConfig.getKeys(), localConfig.getServerGroup());
      case LOCAL:
        return ClientProvider.local(logger, localConfig.getKeys(), localConfig.getServerGroup());
    }

    throw new RuntimeException("Unexpected environment " + localConfig.getEnvironment());
  }
}
