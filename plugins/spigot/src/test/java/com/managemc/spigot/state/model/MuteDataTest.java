package com.managemc.spigot.state.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openapitools.client.model.BanOrMute;
import org.openapitools.client.model.CursoryPlayerInfo;
import org.openapitools.client.model.PunishmentVisibility;

import java.util.UUID;

public class MuteDataTest {

  private UUID uuid;

  @Before
  public void setup() {
    uuid = UUID.randomUUID();
  }

  @Test
  public void init_nullableAttributes() {
    BanOrMute mute = new BanOrMute()
        .externalId("MUTE-123")
        .visibility(PunishmentVisibility.PUBLIC);
    MuteData muteData = new MuteData(uuid, mute);
    Assert.assertEquals(uuid, muteData.getUuid());
    Assert.assertNull(muteData.getReason());
    Assert.assertNull(muteData.getExpiresAt());
    Assert.assertNull(muteData.getIssuerId());
    Assert.assertFalse(muteData.isShadow());
    Assert.assertEquals("MUTE-123", muteData.getId());
  }

  @Test
  public void init_noMissingAttributes() {
    UUID issuerId = UUID.randomUUID();

    long before = System.currentTimeMillis();
    BanOrMute mute = new BanOrMute()
        .externalId("MUTE-123")
        .reason("whatever")
        .duration(10_000L)
        .issuer(new CursoryPlayerInfo().uuid(issuerId))
        .visibility(PunishmentVisibility.SHADOW);
    MuteData muteData = new MuteData(uuid, mute);
    Assert.assertEquals(uuid, muteData.getUuid());
    Assert.assertEquals("whatever", muteData.getReason());
    Assert.assertTrue(muteData.getExpiresAt() > System.currentTimeMillis());
    Assert.assertTrue(muteData.getExpiresAt() <= System.currentTimeMillis() + 10_000);
    Assert.assertTrue(muteData.getExpiresAt() >= before + 10_000);
    Assert.assertEquals(issuerId, muteData.getIssuerId());
    Assert.assertTrue(muteData.isShadow());
    Assert.assertEquals("MUTE-123", muteData.getId());
  }
}
