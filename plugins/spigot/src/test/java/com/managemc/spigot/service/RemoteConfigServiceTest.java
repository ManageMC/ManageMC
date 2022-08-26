package com.managemc.spigot.service;

import com.managemc.api.ApiException;
import com.managemc.spigot.testutil.TestBase;
import org.junit.Assert;
import org.junit.Test;

public class RemoteConfigServiceTest extends TestBase {

  @Test
  public void loadSync() throws ApiException {
    Assert.assertNull(config.getRemoteConfigService().getRemoteConfig());
    config.getRemoteConfigService().loadSync();
    Assert.assertNotNull(config.getRemoteConfigService().getRemoteConfig().getBlockedCommandsForMutedPlayers());
  }
}
