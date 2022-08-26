package com.managemc.spigot.task;

import com.managemc.api.ApiException;
import com.managemc.spigot.service.HeartbeatService;
import com.managemc.spigot.testutil.TestBase;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;

public class PeriodicHeartbeatSenderTest extends TestBase {

  private static final String OK_VERSION = "0.0.1";
  private static final String BAD_VERSION = "0.0.0";

  private static final VerificationMode NEVER = Mockito.never();
  private static final VerificationMode ONCE = Mockito.times(1);

  @Test
  public void start_whenOnTheMinute_shouldStartImmediately() {
    PeriodicHeartbeatSender sender = Mockito.spy(config.getPeriodicHeartbeatSender());
    Mockito.when(sender.currentTime()).thenReturn(0L);

    sender.start();
    Mockito.verify(sender).start(0L);
  }

  @Test
  public void start_whenOnAnIncrement_shouldStartImmediately() {
    PeriodicHeartbeatSender sender = Mockito.spy(config.getPeriodicHeartbeatSender());
    Mockito.when(sender.currentTime()).thenReturn(20000L);

    sender.start();
    Mockito.verify(sender).start(0L);
  }

  @Test
  public void start_whenNotOnAnIncrement_shouldWaitBeforeStarting() {
    PeriodicHeartbeatSender sender = Mockito.spy(config.getPeriodicHeartbeatSender());
    Mockito.when(sender.currentTime()).thenReturn(20001L);

    sender.start();
    Mockito.verify(sender).start(19999L);
  }

  @Test
  public void happyPath() throws ApiException {
    config.getHeartbeatService().emitInitialHeartbeat();
    awaitWebServiceResponse(() -> config.getPeriodicHeartbeatSender().startImmediately());
    Mockito.verify(config.getLogging(), NEVER).logWarning(Mockito.any());
    Mockito.verify(config.getLogging(), NEVER).logSevere(Mockito.any());
    Mockito.verify(config.getBukkitWrapper(), NEVER).shutdown();

    config.getPeriodicHeartbeatSender().stop();
  }

  @Test
  public void slightlyOutOfDate() throws ApiException {
    config.getHeartbeatService().emitInitialHeartbeat();
    config.getLocalConfig().setVersion(OK_VERSION);
    awaitWebServiceResponse(() -> config.getPeriodicHeartbeatSender().startImmediately());
    Mockito.verify(config.getLogging(), ONCE).logWarning(HeartbeatService.OUT_OF_DATE_MESSAGE);
    Mockito.verify(config.getLogging(), NEVER).logSevere(Mockito.any());
    Mockito.verify(config.getBukkitWrapper(), NEVER).shutdown();

    config.getPeriodicHeartbeatSender().stop();
  }

  @Test
  public void veryOutOfDate() throws ApiException {
    config.getHeartbeatService().emitInitialHeartbeat();
    config.getLocalConfig().setVersion(BAD_VERSION);
    awaitWebServiceResponse(() -> config.getPeriodicHeartbeatSender().startImmediately());
    Mockito.verify(config.getLogging(), NEVER).logWarning(Mockito.any());
    Mockito.verify(config.getLogging(), ONCE).logSevere(HeartbeatService.UNSUPPORTED_MESSAGE);
    Mockito.verify(config.getBukkitWrapper(), ONCE).shutdown();

    config.getPeriodicHeartbeatSender().stop();
  }
}
