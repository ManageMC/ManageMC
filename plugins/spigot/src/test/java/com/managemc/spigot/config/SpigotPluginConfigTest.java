package com.managemc.spigot.config;

import com.managemc.api.wrapper.ClientProvider;
import com.managemc.spigot.testutil.TestMocks;

public class SpigotPluginConfigTest extends SpigotPluginConfigLocal {

  public SpigotPluginConfigTest(TestMocks mocks, ClientProvider webClients) {
    super(mocks.getLogging(), mocks.getBukkitWrapper(), mocks.getLocalConfig(), mocks.getPlayerData(), webClients);
  }
}
