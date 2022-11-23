package com.managemc.spigot.command.processor;

import com.managemc.spigot.command.util.CommandAssertions;
import com.managemc.spigot.command.util.CommandNodeMap;
import com.managemc.spigot.command.util.CommandUsage;
import com.managemc.spigot.testutil.TestBase;
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
    onCommand("oops");
    assertSyntactic(CommandAssertions.WRONG_NUM_ARGS, CommandUsage.WATCHLIST_SHOW);
  }

  @Test
  public void whenSender_isNotPlayer() {
    sender = Mockito.mock(CommandSender.class);
    onCommand();
    assertSemantic(CommandAssertions.ONLY_PLAYERS_MESSAGE);
  }

  @Test
  public void whenSender_noPermissions() {
    stubPlayerSenderWithPermissions(PHYLLIS_UUID, Permission.BAN_WEB);
    onCommand();
    assertSemantic(CommandNodeMap.NO_PERMS_MSG);
  }

  @Test
  public void success_emptyList() {
    stubPlayerSenderWithPermissions(PLS_UUID, Permission.MANAGE_WATCHLIST);
    awaitAsyncCommand(this::onCommand);
    Mockito.verify(sender).sendMessage(Mockito.contains(WatchlistFormatter.EMPTY_LIST));
    Mockito.verify(sender, Mockito.times(1)).sendMessage(Mockito.anyString());
  }

  @Captor
  private ArgumentCaptor<String> message;

  @Test
  public void success_withElements() {
    awaitAsyncCommand(this::onCommand);
    Mockito.verify(sender).sendMessage(message.capture());
    Mockito.verify(sender, Mockito.times(1)).sendMessage(Mockito.anyString());

    Assert.assertTrue(message.getValue().contains("Online:"));
    Assert.assertTrue(message.getValue().contains("AuntPhyllis"));
    Assert.assertFalse(message.getValue().contains("AuntPhyllis:"));
  }


  private void onCommand(String... args) {
    String[] argsIncludingBaseCommand = new String[2 + args.length];
    argsIncludingBaseCommand[0] = "watchlist";
    argsIncludingBaseCommand[1] = "show";
    System.arraycopy(args, 0, argsIncludingBaseCommand, 2, argsIncludingBaseCommand.length - 2);
    new CmdMMC(config).onCommand(sender, argsIncludingBaseCommand);
  }
}
