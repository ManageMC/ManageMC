package com.managemc.spigot.command.util;

import com.managemc.spigot.testutil.TestBase;
import com.managemc.spigot.util.permissions.Permission;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

public class CommandNodeMapTest extends TestBase {

  private static final String BASE_OPTIONS = "[bans, ipbans, mutes, players, punishments, warnings, watchlist]";

  private CommandNodeMap nodeMap;

  @Before
  public void before() {
    nodeMap = new CommandNodeMap(config);
  }

  @Test
  public void invalid_noArgs() {
    assertThrowsException(
        "Please supply an action or resource to your command. Valid options are: " + BASE_OPTIONS,
        () -> nodeMap.searchForHandler(sender, new String[]{})
    );
  }

  @Test
  public void invalid_noArgs_playerSender_withNoPermissions() {
    stubPlayerSenderWithPermissions();

    assertThrowsException(
        "Please supply an action or resource to your command. Valid options are: [players]",
        () -> nodeMap.searchForHandler(sender, new String[]{})
    );
  }

  @Test
  public void invalid_oneArg() {
    assertThrowsException(
        "Invalid keyword \"oops\". Valid option(s) are: " + BASE_OPTIONS,
        () -> getHandlerUsage("oops")
    );
  }

  @Test
  public void invalid_oneArg_playerSender_withNoPermissions() {
    stubPlayerSenderWithPermissions();

    assertThrowsException(
        "Invalid keyword \"oops\". Valid option(s) are: [players]",
        () -> getHandlerUsage("oops")
    );
  }

  @Test
  public void invalid_terminalWord() {
    assertThrowsException(
        "Invalid keyword \"oops\". Valid option(s) are: [get, pardon]",
        () -> getHandlerUsage("punishments oops")
    );
  }

  @Test
  public void invalid_terminalWord_playerSender_withLimitedPermissions() {
    stubPlayerSenderWithPermissions(Permission.PUNISHMENTS_VIEW);

    assertThrowsException(
        "Invalid keyword \"oops\". Valid option(s) are: [get]",
        () -> getHandlerUsage("punishments oops")
    );
  }

  @Test
  public void ambiguousArgument() {
    assertThrowsException(
        "Ambiguous keyword \"p\". Matching options are: [players, punishments]",
        () -> getHandlerUsage("p")
    );
  }

  @Test
  public void ambiguousArgument_playerSender_withNoPermissions() {
    stubPlayerSenderWithPermissions();

    assertThrowsException(
        "Ambiguous keyword \"p\". Matching options are: [players]",
        () -> getHandlerUsage("p")
    );
  }

  @Test
  public void punishments_pardon_noOtherArgs() {
    Assert.assertEquals(CommandUsage.PUNISHMENTS_PARDON, getHandlerUsage("punishments pardon"));
  }

  @Test
  public void punishments_pardon_multipleArgs() {
    Assert.assertEquals(CommandUsage.PUNISHMENTS_PARDON, getHandlerUsage("punishments pardon etc etc"));
  }

  @Test
  public void punishments_pardon_abbreviated() {
    Assert.assertEquals(CommandUsage.PUNISHMENTS_PARDON, getHandlerUsage("pun par etc etc"));
  }

  @Test
  public void bans_create_noOtherArgs() {
    Assert.assertEquals(CommandUsage.BANS_CREATE, getHandlerUsage("bans create"));
  }

  @Test
  public void onTabComplete_noArgs() {
    Assert.assertEquals(BASE_OPTIONS, onTabComplete());
  }

  @Test
  public void onTabComplete_oneArg() {
    Assert.assertEquals("[punishments]", onTabComplete("punishments"));
  }

  @Test
  public void onTabComplete_oneArg_trailingSpace() {
    Assert.assertEquals("[get, pardon]", onTabComplete("punishments", ""));
  }

  @Test
  public void onTabComplete_oneArg_unambiguousPartialMatch() {
    Assert.assertEquals("[punishments]", onTabComplete("pun"));
  }

  @Test
  public void onTabComplete_oneArg_unambiguousPartialMatch_withExtraSpace() {
    Assert.assertEquals("[get, pardon]", onTabComplete("pun", ""));
  }

  @Test
  public void onTabComplete_oneArg_ambiguousPartialMatch() {
    Assert.assertEquals("[players, punishments]", onTabComplete("p"));
  }

  @Test
  public void onTabComplete_oneArg_ambiguousPartialMatch_withExtraSpace() {
    Assert.assertEquals("[]", onTabComplete("p", ""));
  }

  @Test
  public void onTabComplete_twoArgs_fullMatch() {
    Assert.assertEquals("[get]", onTabComplete("punishments", "get"));
  }

  @Test
  public void onTabComplete_twoArgs_fullMatch_whitespaceInTheMiddle() {
    Assert.assertEquals("[get]", onTabComplete("punishments", "", "get"));
  }

  @Test
  public void onTabComplete_twoArgs_fullMatch_trailingSpace() {
    stubOnlinePlayers("player123");

    Assert.assertEquals("[player123]", onTabComplete("punishments", "get", ""));
  }

  @Test
  public void onTabComplete_twoArgs_fullMatch_bunchOfWhitespaceChars() {
    stubOnlinePlayers("player123");

    Assert.assertEquals("[player123]", onTabComplete("punishments", "get", "", "", ""));
  }

  @Test
  public void onTabComplete_twoArgs_partialMatch() {
    Assert.assertEquals("[get]", onTabComplete("punishments", "g"));
  }

  @Test
  public void onTabComplete_twoArgs_partialMatches_bunchOfWhitespaceChars() {
    stubOnlinePlayers("player123");

    Assert.assertEquals("[player123]", onTabComplete("pu", "", "", "g", "", "", ""));
  }

  @Test
  public void onTabComplete_threeArgs_noPlayerMatch() {
    stubOnlinePlayers("player123");

    Assert.assertEquals("[]", onTabComplete("punishments", "get", "oops"));
  }

  @Test
  public void onTabComplete_twoArgs_withARandomEqualsSign() {
    stubOnlinePlayers("player123");

    Assert.assertEquals("[]", onTabComplete("punishments", "get", "="));
  }

  @Test
  public void onTabComplete_threeArgs_partialPlayerMatch() {
    stubOnlinePlayers("player123");

    Assert.assertEquals("[player123]", onTabComplete("punishments", "get", "pla"));
  }

  @Test
  public void onTabComplete_threeArgs_fullPlayerMatch() {
    stubOnlinePlayers("player123");

    Assert.assertEquals("[player123]", onTabComplete("punishments", "get", "player123"));
  }

  @Test
  public void onTabComplete_threeArgs_fullPlayerMatch_trailingSpace() {
    stubOnlinePlayers("player123");

    Assert.assertEquals("[]", onTabComplete("punishments", "get", "player123", ""));
  }

  @Test
  public void onTabComplete_threeArgs_partialPlayerMatch_trailingSpace() {
    stubOnlinePlayers("player123");

    Assert.assertEquals("[]", onTabComplete("punishments", "get", "pla", ""));
  }

  @Test
  public void onTabComplete_playerSender_noPermissions_noArgs() {
    stubPlayerSenderWithPermissions();

    Assert.assertEquals("[players]", onTabComplete());
  }

  @Test
  public void onTabComplete_playerSender_noPermissions_oneArg_partialMatch() {
    stubPlayerSenderWithPermissions();

    Assert.assertEquals("[players]", onTabComplete("p"));
  }

  @Test
  public void onTabComplete_playerSender_noPermissions_oneArg_fullMatch() {
    stubPlayerSenderWithPermissions();

    Assert.assertEquals("[]", onTabComplete("punishments", ""));
  }

  @Test
  public void onTabComplete_playerSender_punishmentsGetPermissionOnly() {
    stubPlayerSenderWithPermissions(Permission.PUNISHMENTS_VIEW);

    Assert.assertEquals("[get]", onTabComplete("punishments", ""));
    Assert.assertEquals("[]", onTabComplete("punishments", "p"));
  }

  @Test
  public void onTabComplete_playerSender_punishmentsPardonPermissionOnly() {
    stubPlayerSenderWithPermissions(Permission.BAN_IN_GAME);

    Assert.assertEquals("[pardon]", onTabComplete("punishments", ""));
    Assert.assertEquals("[]", onTabComplete("punishments", "g"));
  }

  @Test
  public void onTabComplete_playerSender_adminPermissions() {
    stubPlayerSenderWithPermissions(Permission.ADMIN);

    Assert.assertEquals("[get, pardon]", onTabComplete("punishments", ""));
  }


  private void assertThrowsException(String message, ThrowingRunnable runnable) {
    CommandValidationException ex = Assert.assertThrows(CommandValidationException.class, runnable);
    Assert.assertEquals(message, ex.getMessage());
  }

  /**
   * The usage just makes a convenient ID
   */
  private String getHandlerUsage(String args) {
    return nodeMap.searchForHandler(sender, args.split(" ")).getProcessor().usage;
  }
}
