package com.managemc.importer;

import com.managemc.api.ApiException;
import com.managemc.api.wrapper.ClientProvider;
import com.managemc.plugins.command.CommandExecutorAsync;
import org.junit.Before;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

public abstract class TestsWithServiceClient {

  @Before
  public void assertGoodClientConnection() throws ApiException {
    // make sure that we have connected to the service at least once
    // the first request takes a while a execute
    // subsequent requests are fast
    // this ensures that we have made the "slow" request before any tests are run
    // we also need to make sure we only run this once for the entire test suite
    if (!(ClientProvider.COMPLETED_REQUESTS.get() > 0)) {
      TestWebClients.CLIENT_PROVIDER.externalApplication().getPingApi().ping();
    }
  }

  protected void awaitAsyncCommand(VoidFunctionPossiblyThrowingException function) {
    long completedRequests = CommandExecutorAsync.COMPLETED_COMMANDS.get();
    try {
      function.apply();
    } catch (Exception e) {
      e.printStackTrace();
    }
    await().atMost(20, TimeUnit.SECONDS).until(() -> CommandExecutorAsync.COMPLETED_COMMANDS.get() > completedRequests);
  }

  @FunctionalInterface
  protected interface VoidFunctionPossiblyThrowingException {
    void apply() throws Exception;
  }
}
