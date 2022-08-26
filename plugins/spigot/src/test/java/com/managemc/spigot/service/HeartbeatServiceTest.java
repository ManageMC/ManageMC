package com.managemc.spigot.service;

import com.managemc.api.ApiException;
import com.managemc.spigot.testutil.TestBase;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;

public class HeartbeatServiceTest extends TestBase {

  private static final String OK_VERSION = "0.0.1";
  private static final String BAD_VERSION = "0.0.0";

  private static final VerificationMode NEVER = Mockito.never();
  private static final VerificationMode ONCE = Mockito.times(1);

  @Test
  public void initialHeartbeat_goodVersion() throws ApiException {
    config.getHeartbeatService().emitInitialHeartbeat();
    Mockito.verify(config.getLogging(), NEVER).logWarning(Mockito.any());
    Mockito.verify(config.getLogging(), NEVER).logSevere(Mockito.any());
    Mockito.verify(config.getBukkitWrapper(), NEVER).shutdown();
  }

  @Test
  public void initialHeartbeat_slightlyOutOfDate() throws ApiException {
    config.getLocalConfig().setVersion(OK_VERSION);
    config.getHeartbeatService().emitInitialHeartbeat();
    Mockito.verify(config.getLogging(), ONCE).logWarning(HeartbeatService.OUT_OF_DATE_MESSAGE);
    Mockito.verify(config.getLogging(), NEVER).logSevere(Mockito.any());
    Mockito.verify(config.getBukkitWrapper(), NEVER).shutdown();
  }

  @Test
  public void initialHeartbeat_veryOutOfDate() throws ApiException {
    config.getLocalConfig().setVersion(BAD_VERSION);
    config.getHeartbeatService().emitInitialHeartbeat();
    Mockito.verify(config.getLogging(), NEVER).logWarning(Mockito.any());
    Mockito.verify(config.getLogging(), ONCE).logSevere(HeartbeatService.UNSUPPORTED_MESSAGE);
    Mockito.verify(config.getBukkitWrapper(), ONCE).shutdown();
  }

  @Test
  public void periodicHeartbeatTime_whenOnTheHour_returnsCurrentTime() {
    HeartbeatService service = Mockito.spy(config.getHeartbeatService());
    Mockito.when(service.currentTime()).thenReturn(0L);

    Assert.assertEquals(0, service.periodicHeartbeatTime());
  }

  @Test
  public void periodicHeartbeatTime_whenOnIncrement_returnsCurrentTime() {
    HeartbeatService service = Mockito.spy(config.getHeartbeatService());
    Mockito.when(service.currentTime()).thenReturn(20000L);

    Assert.assertEquals(20000, service.periodicHeartbeatTime());
  }

  @Test
  public void periodicHeartbeatTime_whenNotOnIncrement_returnsMostRecentIncrementTime() {
    HeartbeatService service = Mockito.spy(config.getHeartbeatService());
    Mockito.when(service.currentTime()).thenReturn(20001L);

    Assert.assertEquals(20000, service.periodicHeartbeatTime());
  }

  @Test
  public void heartbeat_goodVersion() throws ApiException {
    config.getHeartbeatService().emitInitialHeartbeat();
    config.getHeartbeatService().emitHeartbeat();
    Mockito.verify(config.getLogging(), NEVER).logWarning(Mockito.any());
    Mockito.verify(config.getLogging(), NEVER).logSevere(Mockito.any());
    Mockito.verify(config.getBukkitWrapper(), NEVER).shutdown();
  }

  @Test
  public void heartbeat_slightlyOutOfDate() throws ApiException {
    config.getHeartbeatService().emitInitialHeartbeat();
    config.getLocalConfig().setVersion(OK_VERSION);
    config.getHeartbeatService().emitHeartbeat();
    Mockito.verify(config.getLogging(), ONCE).logWarning(HeartbeatService.OUT_OF_DATE_MESSAGE);
    Mockito.verify(config.getLogging(), NEVER).logSevere(Mockito.any());
    Mockito.verify(config.getBukkitWrapper(), NEVER).shutdown();
  }

  @Test
  public void heartbeat_veryOutOfDate() throws ApiException {
    config.getHeartbeatService().emitInitialHeartbeat();
    config.getLocalConfig().setVersion(BAD_VERSION);
    config.getHeartbeatService().emitHeartbeat();
    Mockito.verify(config.getLogging(), NEVER).logWarning(Mockito.any());
    Mockito.verify(config.getLogging(), ONCE).logSevere(HeartbeatService.UNSUPPORTED_MESSAGE);
    Mockito.verify(config.getBukkitWrapper(), ONCE).shutdown();
  }

  @Test
  public void finalHeartbeat_happyPath() throws ApiException {
    config.getHeartbeatService().emitInitialHeartbeat();
    config.getHeartbeatService().emitFinalHeartbeat();
  }
}
