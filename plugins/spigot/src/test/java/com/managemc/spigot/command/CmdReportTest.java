package com.managemc.spigot.command;

import com.managemc.api.wrapper.model.metadata.ExternalServerAuthMetadata;
import com.managemc.plugins.command.CommandExecutorAsync;
import com.managemc.spigot.command.CmdReport;
import com.managemc.spigot.command.util.CommandAssertions;
import com.managemc.spigot.command.util.punishments.model.ResolvedPlayer;
import com.managemc.spigot.testutil.TestBase;
import com.managemc.spigot.testutil.TestConstants;
import lombok.SneakyThrows;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openapitools.client.model.CreateWatchRequestInput;

import java.util.UUID;

public class CmdReportTest extends TestBase {

  @Mock
  private Location senderLocation;
  @Mock
  private Location accusedLocation;

  private UUID expectedUuid;
  private String expectedSummary;
  private Integer expectedDistance;

  private static final int DISTANCE = 234;

  @Before
  public void setup() {
    sender = Mockito.mock(Player.class);
    Mockito.when(sender.getName()).thenReturn(PHYLLIS_USERNAME);
    Mockito.when(((Player) sender).getUniqueId()).thenReturn(PHYLLIS_UUID);
    Mockito.when(((Player) sender).getLocation()).thenReturn(senderLocation);
    Mockito.when(senderLocation.distance(accusedLocation)).thenReturn((double) DISTANCE);
    Mockito.when(accusedLocation.distance(senderLocation)).thenReturn((double) DISTANCE);

    expectedUuid = JACOB_UUID;
    expectedSummary = null;
    expectedDistance = DISTANCE;
  }

  @Test
  public void notEnoughArgs() {
    onCommand(false);
    expectAbort(CommandAssertions.WRONG_NUM_ARGS);
  }

  @Test
  public void whenSender_isNotPlayer() {
    sender = Mockito.mock(CommandSender.class);
    onCommand(true, JACOB_USERNAME);
    expectAbort(CommandAssertions.ONLY_PLAYERS_MESSAGE);
  }

  @Test
  public void invalidUsername() {
    onCommand(false, "@@@");
    expectAbort("Invalid username @@@");
  }

  @Test
  public void selfReport() {
    onCommand(true, "aUntPHylliS");
    expectAbort(CmdReport.NO_SELF_REPORTS);
  }

  @Test
  public void onlineAccused_noReason() {
    stubJacobOnline();
    evaluateNewReport(JACOB_USERNAME);

    Mockito.verify(sender).sendMessage(CmdReport.SUCCESS_MSG);
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
  }

  @Test
  public void onlineAccused_withOneWordReason() {
    stubJacobOnline();
    expectedSummary = "hmmm";

    evaluateNewReport(JACOB_USERNAME, "hmmm");
    Mockito.verify(sender).sendMessage(CmdReport.SUCCESS_MSG);
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
  }

  @Test
  public void onlineAccused_withMultiWordReason() {
    stubJacobOnline();
    expectedSummary = "nobody cares mate";

    evaluateNewReport(JACOB_USERNAME, "nobody", "cares", "mate");
    expectSingleMessage(CmdReport.SUCCESS_MSG);
  }

  @Test
  public void onlineAccused_realExample() {
    stubJacobOnline();
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME));

    expectSingleMessage(CmdReport.SUCCESS_MSG);
  }

  @Test
  public void offlineAccused_fakePlayer() {
    stubResolvedPlayer("ili1i1il_il__i", UUID.randomUUID(), ResolvedPlayer.Status.HTTP_NOT_FOUND);
    awaitAsyncCommand(() -> onCommand(true, "ili1i1il_il__i"));

    expectAbort(CommandAssertions.NOT_FOUND_MSG);
  }

  @Test
  public void offlineAccused_realPlayer_noReason() {
    stubResolvedPlayer(JACOB_USERNAME, JACOB_UUID, ResolvedPlayer.Status.HTTP_OK);

    expectedDistance = null;
    evaluateNewReport(JACOB_USERNAME);
    Mockito.verify(sender).sendMessage(CmdReport.OFFLINE_MSG);
    Mockito.verify(sender).sendMessage(CmdReport.SUCCESS_MSG);
    Mockito.verify(sender, TWICE).sendMessage(Mockito.anyString());
  }

  @Test
  public void offlineAccused_realPlayer_withReason() {
    stubResolvedPlayer(JACOB_USERNAME, JACOB_UUID, ResolvedPlayer.Status.HTTP_OK);

    expectedDistance = null;
    expectedSummary = "ayy lmao";
    evaluateNewReport(JACOB_USERNAME, "ayy", "lmao");
    Mockito.verify(sender).sendMessage(CmdReport.OFFLINE_MSG);
    Mockito.verify(sender).sendMessage(CmdReport.SUCCESS_MSG);
    Mockito.verify(sender, TWICE).sendMessage(Mockito.anyString());
  }

  @Test
  public void offlineAccused_realExample() {
    stubResolvedPlayer(JACOB_USERNAME, JACOB_UUID, ResolvedPlayer.Status.HTTP_OK);

    expectedDistance = null;
    awaitAsyncCommand(() -> onCommand(true, JACOB_USERNAME));

    Mockito.verify(sender).sendMessage(CmdReport.OFFLINE_MSG);
    Mockito.verify(sender).sendMessage(CmdReport.SUCCESS_MSG);
    Mockito.verify(sender, TWICE).sendMessage(Mockito.anyString());
  }


  private void stubJacobOnline() {
    Player player = Mockito.mock(Player.class);
    Mockito.when(player.getName()).thenReturn(TestBase.JACOB_USERNAME);
    Mockito.when(player.getUniqueId()).thenReturn(TestBase.JACOB_UUID);
    Mockito.when(player.getLocation()).thenReturn(accusedLocation);

    Mockito.when(config.getBukkitWrapper().getOnlinePlayer(TestBase.JACOB_USERNAME)).thenReturn(player);
  }

  @SneakyThrows
  private void evaluateNewReport(String... args) {
    config.setClientProvider(mockClients);
    ExternalServerAuthMetadata authMetadata = Mockito.mock(ExternalServerAuthMetadata.class);
    Mockito.when(authMetadata.getServerGroupId()).thenReturn(TestConstants.KITPVP_ID);
    Mockito.when(authMetadata.getServerNetworkId()).thenReturn(TestConstants.NETWORK_ID);
    Mockito.when(config.getClientProvider().externalServer().getAuthMetadata()).thenReturn(authMetadata);

    awaitAsyncCommand(() -> onCommand(true, args));

    CreateWatchRequestInput expected = new CreateWatchRequestInput()
        .accuseeUuid(expectedUuid)
        .summary(expectedSummary)
        .inGameDistance(expectedDistance)
        .serverGroupId(TestConstants.KITPVP_ID)
        .serverNetworkId(TestConstants.NETWORK_ID);
    Mockito.verify(playerClient.getAccusationsApi()).createWatchRequest(expected);
  }

  private void onCommand(boolean noSyntaxError, String... args) {
    CommandExecutorAsync cmd = new CmdReport(config);
    Assert.assertEquals(noSyntaxError, cmd.onCommand(sender, command, "", args));
  }
}
