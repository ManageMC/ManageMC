package com.managemc.spigot.state;

import com.managemc.spigot.state.model.MuteData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openapitools.client.model.BanOrMute;
import org.openapitools.client.model.PunishmentVisibility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MuteManagerTest {

  private UUID uuid;
  private MuteManager muteManager;

  @Before
  public void setup() {
    muteManager = new MuteManager();
    uuid = UUID.randomUUID();
  }

  @Test
  public void basicAddGetRemove() {
    BanOrMute request = new BanOrMute();
    MuteData muteData = new MuteData(uuid, request);
    muteManager.setMuted(muteData);
    Assert.assertEquals(muteData, muteManager.getMuteData(uuid));
    muteManager.removeMute(UUID.randomUUID());
    Assert.assertEquals(muteData, muteManager.getMuteData(uuid));
    muteManager.removeMute(uuid);
    Assert.assertNull(muteManager.getMuteData(uuid));
  }

  @Test
  public void getMuteData_whenPresentButExpired() {
    BanOrMute request = new BanOrMute()
        .duration(-1L);
    MuteData muteData = new MuteData(uuid, request);
    muteManager.setMuted(muteData);
    Assert.assertNull(muteManager.getMuteData(uuid));
  }

  @Test
  public void setMuted_originalExpired_shouldOverride() {
    BanOrMute originalMute = new BanOrMute()
        .visibility(PunishmentVisibility.SHADOW)
        .duration(-1L);
    muteManager.setMuted(new MuteData(uuid, originalMute));

    BanOrMute newMute = new BanOrMute()
        .reason("new");
    muteManager.setMuted(new MuteData(uuid, newMute));

    MuteData muteData = muteManager.getMuteData(uuid);
    Assert.assertNotNull(muteData);
    Assert.assertEquals("new", muteData.getReason());
  }

  @Test
  public void setMuted_newIsShadowAndOldIsNot_shouldOverride() {
    BanOrMute originalMute = new BanOrMute()
        .visibility(PunishmentVisibility.PRIVATE);
    muteManager.setMuted(new MuteData(uuid, originalMute));

    BanOrMute newMute = new BanOrMute()
        .visibility(PunishmentVisibility.SHADOW)
        .duration(1_000L)
        .reason("new");
    muteManager.setMuted(new MuteData(uuid, newMute));

    MuteData muteData = muteManager.getMuteData(uuid);
    Assert.assertNotNull(muteData);
    Assert.assertEquals("new", muteData.getReason());
  }

  @Test
  public void setMuted_newWillLastLongerThanOld_shouldOverride() {
    BanOrMute originalMute = new BanOrMute()
        .duration(1_000L)
        .visibility(PunishmentVisibility.SHADOW);
    muteManager.setMuted(new MuteData(uuid, originalMute));

    BanOrMute newMute = new BanOrMute()
        .duration(2_000L)
        .reason("new");
    muteManager.setMuted(new MuteData(uuid, newMute));

    MuteData muteData = muteManager.getMuteData(uuid);
    Assert.assertNotNull(muteData);
    Assert.assertEquals("new", muteData.getReason());
  }

  @Test
  public void setMuted_oldLastsLongerThanNew_shouldNotOverride() {
    BanOrMute originalMute = new BanOrMute()
        .duration(2_000L)
        .reason("old");
    muteManager.setMuted(new MuteData(uuid, originalMute));

    BanOrMute newMute = new BanOrMute()
        .duration(1_000L);
    muteManager.setMuted(new MuteData(uuid, newMute));

    MuteData muteData = muteManager.getMuteData(uuid);
    Assert.assertNotNull(muteData);
    Assert.assertEquals("old", muteData.getReason());
  }

  @Test
  public void removeMutes() {
    UUID uuid1 = UUID.randomUUID();
    UUID uuid2 = UUID.randomUUID();
    UUID uuid3 = UUID.randomUUID();

    muteManager.setMuted(new MuteData(uuid1, new BanOrMute().visibility(PunishmentVisibility.SHADOW).externalId("MUTE-1")));
    muteManager.setMuted(new MuteData(uuid2, new BanOrMute().visibility(PunishmentVisibility.PUBLIC).externalId("MUTE-2")));
    muteManager.setMuted(new MuteData(uuid3, new BanOrMute().visibility(PunishmentVisibility.PUBLIC).externalId("MUTE-3")));

    List<UUID> uuids = new ArrayList<>(muteManager.removeMutes(Arrays.asList("mute-1", "MUTE-2", "BAN-1")));

    Assert.assertEquals(1, uuids.size());
    Assert.assertEquals(uuid2, uuids.get(0));

    Assert.assertNull(muteManager.getMuteData(uuid1));
    Assert.assertNull(muteManager.getMuteData(uuid2));
    Assert.assertNotNull(muteManager.getMuteData(uuid3));
  }
}
