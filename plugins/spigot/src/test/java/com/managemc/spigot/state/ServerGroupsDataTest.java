package com.managemc.spigot.state;

import com.managemc.api.ApiException;
import com.managemc.spigot.testutil.TestBase;
import com.managemc.spigot.testutil.TestWebClients;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ServerGroupsDataTest extends TestBase {

  private ServerGroupsData serverGroupsData;

  @Before
  public void setup() {
    serverGroupsData = new ServerGroupsData(TestWebClients.CLIENT_PROVIDER);
  }

  @Test
  public void getServerGroup_withNoData() {
    Assert.assertTrue(serverGroupsData.isNeedsRefresh());
    Assert.assertNull(serverGroupsData.getServerGroup(1));
    Assert.assertTrue(serverGroupsData.isNeedsRefresh());
  }

  @Test
  public void getServerGroup_withData() throws ApiException {
    serverGroupsData.refreshBlocking();
    Assert.assertFalse(serverGroupsData.isNeedsRefresh());
    Assert.assertEquals("KitPvP", serverGroupsData.getServerGroup(1).getLabel());
    Assert.assertFalse(serverGroupsData.isNeedsRefresh());

    // make it think it needs to refresh again
    Assert.assertNull(serverGroupsData.getServerGroup(0));
    Assert.assertTrue(serverGroupsData.isNeedsRefresh());
  }
}
