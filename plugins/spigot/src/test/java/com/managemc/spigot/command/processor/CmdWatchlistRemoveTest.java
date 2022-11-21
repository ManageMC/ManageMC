package com.managemc.spigot.command.processor;

import com.managemc.spigot.command.util.CommandAssertions;
import com.managemc.spigot.command.util.CommandNodeMap;
import com.managemc.spigot.command.util.CommandUsage;
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

public class CmdWatchlistRemoveTest extends TestBase {

  @Before
  public void setup() {
    stubPlayerSenderWithPermissions(JACOB_UUID, Permission.MANAGE_WATCHLIST);
  }

  @Test
  public void notEnoughArgs() {
    onCommand();
    assertSyntactic(CommandAssertions.WRONG_NUM_ARGS, CommandUsage.WATCHLIST_REMOVE);
  }

  @Test
  public void tooManyArgs() {
    onCommand("oops", "lmao");
    assertSyntactic(CommandAssertions.WRONG_NUM_ARGS, CommandUsage.WATCHLIST_REMOVE);
  }

  @Test
  public void whenSender_isNotPlayer() {
    sender = Mockito.mock(CommandSender.class);
    onCommand("example_player");
    assertSemantic(CommandAssertions.ONLY_PLAYERS_MESSAGE);
  }

  @Test
  public void invalidUsername() {
    onCommand("@@@");
    assertSyntactic("Invalid username @@@", CommandUsage.WATCHLIST_REMOVE);
  }

  @Test
  public void noPermissions() {
    stubPlayerSenderWithPermissions(PHYLLIS_UUID, Permission.BAN_WEB);
    onCommand();
    assertSemantic(CommandNodeMap.NO_PERMS_MSG);
  }

  @Test
  public void playerNotFound() {
    stubResolvedPlayer("soup", UUID.randomUUID(), ResolvedPlayer.Status.ONLINE_RECENTLY);
    awaitAsyncCommand(() -> onCommand("soup"));

    assertSemantic(WatchlistService.PLAYER_NOT_FOUND);
  }

  @Test
  public void playerNotResolved() {
    stubResolvedPlayer("oopsy_da1sy__", UUID.randomUUID(), ResolvedPlayer.Status.HTTP_NOT_FOUND);
    awaitAsyncCommand(() -> onCommand("oopsy_da1sy__"));

    assertSemantic(CommandAssertions.NOT_FOUND_MSG);
  }

  @Captor
  private ArgumentCaptor<String> message;

  @Test
  public void success_online() {
    stubResolvedPlayer(PHYLLIS_USERNAME, PHYLLIS_UUID, ResolvedPlayer.Status.ONLINE_RECENTLY);
    awaitAsyncCommand(() -> this.onCommand(PHYLLIS_USERNAME));

    Mockito.verify(sender).sendMessage(message.capture());
    String sentMessage = message.getValue();
    Assert.assertTrue(sentMessage.contains("Personal Watch List"));
    Assert.assertTrue(sentMessage.contains("Online:"));
    Assert.assertFalse(sentMessage.contains(PHYLLIS_USERNAME));
  }


  private void onCommand(String... args) {
    String[] argsIncludingBaseCommand = new String[2 + args.length];
    argsIncludingBaseCommand[0] = "watchlist";
    argsIncludingBaseCommand[1] = "remove";
    System.arraycopy(args, 0, argsIncludingBaseCommand, 2, argsIncludingBaseCommand.length - 2);
    new CmdMMC(config).onCommand(sender, argsIncludingBaseCommand);
  }
}
