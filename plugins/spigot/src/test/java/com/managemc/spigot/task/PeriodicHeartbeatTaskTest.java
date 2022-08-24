package com.managemc.spigot.task;

import com.managemc.api.ApiException;
import com.managemc.spigot.service.HeartbeatService;
import com.managemc.spigot.testutil.TestBase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;

public class PeriodicHeartbeatTaskTest extends TestBase {

  private static final int MAX_FAILURES = PeriodicHeartbeatTask.MAX_FAILURES_BEFORE_SHUTDOWN;
  private static final String FAILURE_MESSAGE = PeriodicHeartbeatTask.REQUEST_FAILURE_MESSAGE;

  private static final VerificationMode NEVER = Mockito.never();
  private static final VerificationMode ONCE = Mockito.times(1);
  private static final VerificationMode MAX_FAILURES_TIMES = Mockito.times(MAX_FAILURES);

  private PeriodicHeartbeatTask task;

  @Before
  public void setup() {
    task = new PeriodicHeartbeatTask(config.getLogging(), config.getBukkitWrapper(), config.getHeartbeatService());
  }

  @Test
  public void failedToConnectToApi_exactlyMaxTimes() throws ApiException {
    HeartbeatService heartbeatService = Mockito.mock(HeartbeatService.class);
    Exception exception = new ApiException(502, "oops");
    Mockito.doThrow(exception).when(heartbeatService).emitHeartbeat();
    task = new PeriodicHeartbeatTask(config.getLogging(), config.getBukkitWrapper(), heartbeatService);

    for (int i = 0; i < MAX_FAILURES; i++) {
      try {
        task.run();
      } catch (Exception ignored) {
      }
    }

    Mockito.verify(config.getLogging(), MAX_FAILURES_TIMES).logWarning(FAILURE_MESSAGE);
    Mockito.verify(config.getLogging(), MAX_FAILURES_TIMES).logStackTrace(exception);
    Mockito.verify(config.getLogging(), NEVER).logSevere(Mockito.any());
    Mockito.verify(config.getBukkitWrapper(), NEVER).shutdown();
  }

  @Test
  public void failedToConnectToGrpc_oneTooManyTimes() throws ApiException {
    HeartbeatService heartbeatService = Mockito.mock(HeartbeatService.class);
    Exception exception = new ApiException(502, "oops");
    Mockito.doThrow(exception).when(heartbeatService).emitHeartbeat();
    task = new PeriodicHeartbeatTask(config.getLogging(), config.getBukkitWrapper(), heartbeatService);

    for (int i = 0; i < MAX_FAILURES + 1; i++) {
      try {
        task.run();
      } catch (Exception ignored) {
      }
    }

    Mockito.verify(config.getLogging(), MAX_FAILURES_TIMES).logWarning(FAILURE_MESSAGE);
    Mockito.verify(config.getLogging(), Mockito.times(MAX_FAILURES + 1)).logStackTrace(exception);
    Mockito.verify(config.getLogging(), ONCE).logSevere(Mockito.any());
    Mockito.verify(config.getBukkitWrapper(), ONCE).shutdown();
  }
}
