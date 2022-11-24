package com.managemc.spigot.command.processor;

import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.spigot.command.util.CommandAssertions;
import com.managemc.spigot.command.util.punishments.model.ResolvedPlayer;
import com.managemc.spigot.service.WatchlistService;
import com.managemc.spigot.testutil.TestBase;
import com.managemc.spigot.util.permissions.Permission;
import org.bukkit.command.CommandSender;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;

import java.util.UUID;

public class CmdUnwatchTest extends TestBase {

  @Before
  public void setup() {
    stubPlayerSenderWithPermissions(JACOB_UUID, Permission.MANAGE_WATCHLIST);
  }

  @Test
  public void notEnoughArgs() {
    onCommand(false);
    expectAbort(CommandAssertions.WRONG_NUM_ARGS);
  }

  @Test
  public void tooManyArgs() {
    onCommand(false, "oops", "lmao");
    expectAbort(CommandAssertions.WRONG_NUM_ARGS);
  }

  @Test
  public void whenSender_isNotPlayer() {
    sender = Mockito.mock(CommandSender.class);
    onCommand(true, "example_player");
    expectAbort(CommandAssertions.ONLY_PLAYERS_MESSAGE);
  }

  @Test
  public void invalidUsername() {
    onCommand(false, "@@@");
    expectAbort("Invalid username @@@");
  }

  @Test
  public void noPermissions() {
    stubPlayerSenderWithPermissions(PHYLLIS_UUID, Permission.BAN_WEB);
    onCommand(true);
    expectAbort(CommandAssertions.NO_PERMS_MESSAGE);
  }

  @Test
  public void playerNotFound() {
    stubResolvedPlayer("soup", UUID.randomUUID(), ResolvedPlayer.Status.ONLINE_RECENTLY);
    awaitAsyncCommand(() -> onCommand(true, "soup"));

    expectAbort(WatchlistService.PLAYER_NOT_FOUND);
  }

  @Test
  public void playerNotResolved() {
    stubResolvedPlayer("oopsy_da1sy__", UUID.randomUUID(), ResolvedPlayer.Status.HTTP_NOT_FOUND);
    awaitAsyncCommand(() -> onCommand(true, "oopsy_da1sy__"));

    expectAbort(CommandAssertions.NOT_FOUND_MSG);
  }

  @Captor
  private ArgumentCaptor<String> message;

  @Test
  public void success_online() {
    stubResolvedPlayer(PHYLLIS_USERNAME, PHYLLIS_UUID, ResolvedPlayer.Status.ONLINE_RECENTLY);
    awaitAsyncCommand(() -> onCommand(true, PHYLLIS_USERNAME));

    Mockito.verify(sender).sendMessage(message.capture());
    String sentMessage = message.getValue();
    Assert.assertTrue(sentMessage.contains("Personal Watch List"));
    Assert.assertTrue(sentMessage.contains("Online:"));
    Assert.assertFalse(sentMessage.contains(PHYLLIS_USERNAME));
  }


  private void onCommand(boolean noSyntaxError, String... args) {
    CommandExecutorAsync cmd = new CmdUnwatch(config);
    Assert.assertEquals(noSyntaxError, cmd.onCommand(sender, command, "", args));
  }
}
