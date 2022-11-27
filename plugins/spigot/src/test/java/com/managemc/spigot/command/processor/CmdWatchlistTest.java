package com.managemc.spigot.command.processor;

import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.spigot.command.util.CommandAssertions;
import com.managemc.spigot.testutil.TestBase;
import com.managemc.spigot.testutil.TestConstants;
import com.managemc.spigot.util.chat.formatter.WatchlistFormatter;
import com.managemc.spigot.util.permissions.Permission;
import org.bukkit.command.CommandSender;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;

public class CmdWatchlistTest extends TestBase {

  @Before
  public void setup() {
    stubPlayerSenderWithPermissions(JACOB_UUID, Permission.MANAGE_WATCHLIST);
  }

  @Test
  public void tooManyArgs() {
    onCommand(false, "oops");
    expectAbort(CommandAssertions.WRONG_NUM_ARGS);
  }

  @Test
  public void whenSender_isNotPlayer() {
    sender = Mockito.mock(CommandSender.class);
    onCommand(true);
    expectAbort(CommandAssertions.ONLY_PLAYERS_MESSAGE);
  }

  @Test
  public void whenSender_noPermissions() {
    stubPlayerSenderWithPermissions(PHYLLIS_UUID, Permission.BAN_WEB);
    onCommand(true);
    expectAbort(CommandAssertions.NO_PERMS_MESSAGE);
  }

  @Test
  public void playerSender_noPermsServerSide() {
    stubPlayerSenderWithPermissions(TestConstants.PHYLLIS_UUID, Permission.ADMIN);
    awaitAsyncCommand(() -> onCommand(true));

    expectAbort(CommandAssertions.NO_PERMS_MESSAGE);
    Mockito.verify(config.getLogging(), NEVER).logStackTrace(Mockito.any());
  }

  @Test
  public void success_emptyList() {
    stubPlayerSenderWithPermissions(PLS_UUID, Permission.MANAGE_WATCHLIST);
    awaitAsyncCommand(() -> onCommand(true));
    Mockito.verify(sender).sendMessage(Mockito.contains(WatchlistFormatter.EMPTY_LIST));
    Mockito.verify(sender, Mockito.times(1)).sendMessage(Mockito.anyString());
  }

  @Captor
  private ArgumentCaptor<String> message;

  @Test
  public void success_withElements() {
    awaitAsyncCommand(() -> onCommand(true));
    Mockito.verify(sender).sendMessage(message.capture());
    Mockito.verify(sender, Mockito.times(1)).sendMessage(Mockito.anyString());

    Assert.assertTrue(message.getValue().contains("Online:"));
    Assert.assertTrue(message.getValue().contains("AuntPhyllis"));
    Assert.assertFalse(message.getValue().contains("AuntPhyllis:"));
  }


  private void onCommand(boolean noSyntaxError, String... args) {
    CommandExecutorAsync cmd = new CmdWatchlist(config);
    Assert.assertEquals(noSyntaxError, cmd.onCommand(sender, command, "", args));
  }
}
