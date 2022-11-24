package com.managemc.spigot.command.processor;

import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.plugins.command.CommandWrapping;
import com.managemc.spigot.command.util.CommandAssertions;
import com.managemc.spigot.command.util.punishments.model.ResolvedPlayer;
import com.managemc.spigot.testutil.TestBase;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class CmdCreatePlayerTest extends TestBase {

  public static final String SUCCESS_MESSAGE = String.format(CmdCreatePlayer.SUCCESS_MSG, PHYLLIS_USERNAME);

  @Test
  public void failure_notEnoughArgs() {
    onCommand(false);
    expectAbort(CommandAssertions.WRONG_NUM_ARGS);
  }

  @Test
  public void failure_tooManyArgs() {
    onCommand(false, "a", "b");
    expectAbort(CommandAssertions.WRONG_NUM_ARGS);
  }

  @Test
  public void failure_badUsername() {
    onCommand(false, "!23!@#");
    expectAbort("Invalid username !23!@#");
  }

  @Test
  public void success_onlineRecently() {
    stubOffender(ResolvedPlayer.Status.ONLINE_RECENTLY);
    awaitAsyncCommand(() -> onCommand(true, PHYLLIS_USERNAME));

    expectSingleMessage(SUCCESS_MESSAGE);
  }

  @Test
  public void success_httpOk() {
    stubOffender(ResolvedPlayer.Status.HTTP_OK);
    awaitAsyncCommand(() -> onCommand(true, PHYLLIS_USERNAME));

    expectSingleMessage(SUCCESS_MESSAGE);
  }

  @Test
  public void failure_notFound() {
    stubOffender(ResolvedPlayer.Status.HTTP_NOT_FOUND);
    awaitAsyncCommand(() -> onCommand(true, PHYLLIS_USERNAME));

    expectAbort(CommandAssertions.NOT_FOUND_MSG);
  }

  @Test
  public void failure_rateLimitExceeded() {
    stubOffender(ResolvedPlayer.Status.HTTP_RATE_LIMIT_EXCEEDED);
    awaitAsyncCommand(() -> onCommand(true, PHYLLIS_USERNAME));

    expectAbort(CommandAssertions.MOJANG_RATE_LIMIT_MSG);
  }

  @Test
  public void failure_httpUnexpected() {
    stubOffender(ResolvedPlayer.Status.HTTP_UNEXPECTED);
    awaitAsyncCommand(() -> onCommand(true, PHYLLIS_USERNAME));

    Mockito.verify(sender).sendMessage(CommandWrapping.HARD_FAILURE_MESSAGE);
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(config.getLogging(), ONCE).logStackTrace(Mockito.any());
  }

  @Test
  public void failure_uuidResolverClientError() {
    stubOffender(ResolvedPlayer.Status.CLIENT_EXCEPTION);
    awaitAsyncCommand(() -> onCommand(true, PHYLLIS_USERNAME));

    Mockito.verify(sender).sendMessage(CommandWrapping.HARD_FAILURE_MESSAGE);
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(config.getLogging(), ONCE).logStackTrace(Mockito.any());
  }


  private void stubOffender(ResolvedPlayer.Status httpStatus) {
    stubResolvedPlayer(PHYLLIS_USERNAME, PHYLLIS_UUID, httpStatus);
  }

  private void onCommand(boolean noSyntaxError, String... args) {
    CommandExecutorAsync cmd = new CmdCreatePlayer(config);
    Assert.assertEquals(noSyntaxError, cmd.onCommand(sender, command, "", args));
  }
}
