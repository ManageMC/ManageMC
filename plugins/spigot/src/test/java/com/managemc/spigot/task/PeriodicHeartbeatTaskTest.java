package com.managemc.spigot.task;

import com.managemc.api.ApiException;
import com.managemc.spigot.service.HeartbeatService;
import com.managemc.spigot.testutil.TestBase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;

public class PeriodicHeartbeatTaskTest extends TestBase {

  private static final int MAX_FAILURES = PeriodicHeartbeatTask.MAX_TRANSIENT_FAILURES;
  private static final String TRANSIENT_FAILURE = PeriodicHeartbeatTask.TRANSIENT_FAILURE_MESSAGE;
  private static final String PERSISTENT_FAILURE = PeriodicHeartbeatTask.PERSISTENT_FAILURE_MESSAGE;

  private static final VerificationMode NEVER = Mockito.never();
  private static final VerificationMode ONCE = Mockito.times(1);
  private static final VerificationMode MAX_FAILURES_TIMES = Mockito.times(MAX_FAILURES);

  private PeriodicHeartbeatTask task;

  @Before
  public void setup() {
    task = new PeriodicHeartbeatTask(config.getLogging(), config.getHeartbeatService());
  }

  @Test
  public void failedToConnectToApi_exactlyMaxTimes() throws ApiException {
    HeartbeatService heartbeatService = Mockito.mock(HeartbeatService.class);
    Exception exception = new ApiException(502, "oops");
    Mockito.doThrow(exception).when(heartbeatService).emitHeartbeat();
    task = new PeriodicHeartbeatTask(config.getLogging(), heartbeatService);

    for (int i = 0; i < MAX_FAILURES; i++) {
      try {
        task.run();
      } catch (Exception ignored) {
      }
    }

    Mockito.verify(config.getLogging(), MAX_FAILURES_TIMES).logWarning(TRANSIENT_FAILURE);
    Mockito.verify(config.getLogging(), NEVER).logSevere(PERSISTENT_FAILURE);
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(exception);
  }

  @Test
  public void failedToConnectToApi_oneTooManyTimes() throws ApiException {
    HeartbeatService heartbeatService = Mockito.mock(HeartbeatService.class);
    Exception exception = new ApiException(502, "oops");
    Mockito.doThrow(exception).when(heartbeatService).emitHeartbeat();
    task = new PeriodicHeartbeatTask(config.getLogging(), heartbeatService);

    for (int i = 0; i < MAX_FAILURES + 1; i++) {
      try {
        task.run();
      } catch (Exception ignored) {
      }
    }

    Mockito.verify(config.getLogging(), MAX_FAILURES_TIMES).logWarning(TRANSIENT_FAILURE);
    Mockito.verify(config.getLogging(), ONCE).logSevere(PERSISTENT_FAILURE);
    Mockito.verify(config.getLogging(), ONCE).logStackTrace(exception);
  }
}
