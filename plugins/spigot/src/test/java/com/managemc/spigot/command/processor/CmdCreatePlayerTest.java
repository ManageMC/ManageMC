package com.managemc.spigot.command.processor;

import com.managemc.spigot.command.handler.CmdCreatePlayerHandler;
import com.managemc.spigot.command.util.CommandAssertions;
import com.managemc.spigot.command.util.CommandUsage;
import com.managemc.spigot.command.util.CommandWrapping;
import com.managemc.spigot.command.util.punishments.model.ResolvedPlayer;
import com.managemc.spigot.testutil.TestBase;
import org.bukkit.ChatColor;
import org.junit.Test;
import org.mockito.Mockito;

public class CmdCreatePlayerTest extends TestBase {

  public static final String SUCCESS_MESSAGE = String.format(CmdCreatePlayerHandler.SUCCESS_MSG, PHYLLIS_USERNAME);

  @Test
  public void failure_notEnoughArgs() {
    onCommand();
    assertSyntactic(CommandAssertions.WRONG_NUM_ARGS, CommandUsage.PLAYERS_CREATE);
  }

  @Test
  public void failure_tooManyArgs() {
    onCommand("a", "b");
    assertSyntactic(CommandAssertions.WRONG_NUM_ARGS, CommandUsage.PLAYERS_CREATE);
  }

  @Test
  public void failure_badUsername() {
    onCommand("!23!@#");
    assertSyntactic("Invalid username !23!@#", CommandUsage.PLAYERS_CREATE);
  }

  @Test
  public void success_onlineRecently() {
    stubOffender(ResolvedPlayer.Status.ONLINE_RECENTLY);
    awaitAsyncCommand(() -> onCommand(PHYLLIS_USERNAME));

    Mockito.verify(sender).sendMessage(SUCCESS_MESSAGE);
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
  }

  @Test
  public void success_httpOk() {
    stubOffender(ResolvedPlayer.Status.HTTP_OK);
    awaitAsyncCommand(() -> onCommand(PHYLLIS_USERNAME));

    Mockito.verify(sender).sendMessage(SUCCESS_MESSAGE);
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
  }

  @Test
  public void failure_notFound() {
    stubOffender(ResolvedPlayer.Status.HTTP_NOT_FOUND);
    awaitAsyncCommand(() -> onCommand(PHYLLIS_USERNAME));

    Mockito.verify(sender).sendMessage(ChatColor.RED + CommandAssertions.NOT_FOUND_MSG);
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
  }

  @Test
  public void failure_rateLimitExceeded() {
    stubOffender(ResolvedPlayer.Status.HTTP_RATE_LIMIT_EXCEEDED);
    awaitAsyncCommand(() -> onCommand(PHYLLIS_USERNAME));

    Mockito.verify(sender).sendMessage(ChatColor.RED + CommandAssertions.MOJANG_RATE_LIMIT_MSG);
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
  }

  @Test
  public void failure_httpUnexpected() {
    stubOffender(ResolvedPlayer.Status.HTTP_UNEXPECTED);
    awaitAsyncCommand(() -> onCommand(PHYLLIS_USERNAME));

    Mockito.verify(sender).sendMessage(CommandWrapping.HARD_FAILURE_MESSAGE);
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(config.getLogging(), ONCE).logStackTrace(Mockito.any());
  }

  @Test
  public void failure_uuidResolverClientError() {
    stubOffender(ResolvedPlayer.Status.CLIENT_EXCEPTION);
    awaitAsyncCommand(() -> onCommand(PHYLLIS_USERNAME));

    Mockito.verify(sender).sendMessage(CommandWrapping.HARD_FAILURE_MESSAGE);
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
    Mockito.verify(config.getLogging(), ONCE).logStackTrace(Mockito.any());
  }


  private void stubOffender(ResolvedPlayer.Status httpStatus) {
    stubResolvedPlayer(PHYLLIS_USERNAME, PHYLLIS_UUID, httpStatus);
  }

  private void onCommand(String... args) {
    String[] argsIncludingBaseCommand = new String[2 + args.length];
    argsIncludingBaseCommand[0] = "players";
    argsIncludingBaseCommand[1] = "create";
    System.arraycopy(args, 0, argsIncludingBaseCommand, 2, argsIncludingBaseCommand.length - 2);
    new CmdMMC(config).onCommand(sender, argsIncludingBaseCommand);
  }
}
