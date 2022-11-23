package com.managemc.spigot.command.processor;

import com.managemc.api.wrapper.model.metadata.ExternalServerAuthMetadata;
import com.managemc.spigot.command.handler.CmdReportsCreateHandler;
import com.managemc.spigot.command.util.CommandAssertions;
import com.managemc.spigot.command.util.CommandUsage;
import com.managemc.spigot.command.util.punishments.model.ResolvedPlayer;
import com.managemc.spigot.testutil.TestBase;
import com.managemc.spigot.testutil.TestConstants;
import lombok.SneakyThrows;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openapitools.client.model.CreateWatchRequestInput;

import java.util.UUID;

public class CmdReportsCreateTest extends TestBase {

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
    onCommand();
    assertSyntactic(CommandAssertions.WRONG_NUM_ARGS, CommandUsage.REPORTS_CREATE);
  }

  @Test
  public void whenSender_isNotPlayer() {
    sender = Mockito.mock(CommandSender.class);
    onCommand(JACOB_USERNAME);
    assertSemantic(CommandAssertions.ONLY_PLAYERS_MESSAGE);
  }

  @Test
  public void invalidUsername() {
    onCommand("@@@");
    assertSyntactic("Invalid username @@@", CommandUsage.REPORTS_CREATE);
  }

  @Test
  public void selfReport() {
    onCommand("aUntPHylliS");
    assertSemantic(CmdReportsCreateHandler.NO_SELF_REPORTS);
  }

  @Test
  public void onlineAccused_noReason() {
    stubJacobOnline();
    evaluateNewReport(JACOB_USERNAME);

    Mockito.verify(sender).sendMessage(CmdReportsCreateHandler.SUCCESS_MSG);
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
  }

  @Test
  public void onlineAccused_withOneWordReason() {
    stubJacobOnline();
    expectedSummary = "hmmm";

    evaluateNewReport(JACOB_USERNAME, "hmmm");
    Mockito.verify(sender).sendMessage(CmdReportsCreateHandler.SUCCESS_MSG);
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
  }

  @Test
  public void onlineAccused_withMultiWordReason() {
    stubJacobOnline();
    expectedSummary = "nobody cares mate";

    evaluateNewReport(JACOB_USERNAME, "nobody", "cares", "mate");
    Mockito.verify(sender).sendMessage(CmdReportsCreateHandler.SUCCESS_MSG);
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
  }

  @Test
  public void onlineAccused_realExample() {
    stubJacobOnline();
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME));

    Mockito.verify(sender).sendMessage(CmdReportsCreateHandler.SUCCESS_MSG);
    Mockito.verify(sender, ONCE).sendMessage(Mockito.anyString());
  }

  @Test
  public void offlineAccused_fakePlayer() {
    stubResolvedPlayer("ili1i1il_il__i", UUID.randomUUID(), ResolvedPlayer.Status.HTTP_NOT_FOUND);
    awaitAsyncCommand(() -> onCommand("ili1i1il_il__i"));

    assertSemantic(CommandAssertions.NOT_FOUND_MSG);
  }

  @Test
  public void offlineAccused_realPlayer_noReason() {
    stubResolvedPlayer(JACOB_USERNAME, JACOB_UUID, ResolvedPlayer.Status.HTTP_OK);

    expectedDistance = null;
    evaluateNewReport(JACOB_USERNAME);
    Mockito.verify(sender).sendMessage(CmdReportsCreateHandler.OFFLINE_MSG);
    Mockito.verify(sender).sendMessage(CmdReportsCreateHandler.SUCCESS_MSG);
    Mockito.verify(sender, TWICE).sendMessage(Mockito.anyString());
  }

  @Test
  public void offlineAccused_realPlayer_withReason() {
    stubResolvedPlayer(JACOB_USERNAME, JACOB_UUID, ResolvedPlayer.Status.HTTP_OK);

    expectedDistance = null;
    expectedSummary = "ayy lmao";
    evaluateNewReport(JACOB_USERNAME, "ayy", "lmao");
    Mockito.verify(sender).sendMessage(CmdReportsCreateHandler.OFFLINE_MSG);
    Mockito.verify(sender).sendMessage(CmdReportsCreateHandler.SUCCESS_MSG);
    Mockito.verify(sender, TWICE).sendMessage(Mockito.anyString());
  }

  @Test
  public void offlineAccused_realExample() {
    stubResolvedPlayer(JACOB_USERNAME, JACOB_UUID, ResolvedPlayer.Status.HTTP_OK);

    expectedDistance = null;
    awaitAsyncCommand(() -> onCommand(JACOB_USERNAME));

    Mockito.verify(sender).sendMessage(CmdReportsCreateHandler.OFFLINE_MSG);
    Mockito.verify(sender).sendMessage(CmdReportsCreateHandler.SUCCESS_MSG);
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

    awaitAsyncCommand(() -> onCommand(args));

    CreateWatchRequestInput expected = new CreateWatchRequestInput()
        .accuseeUuid(expectedUuid)
        .summary(expectedSummary)
        .inGameDistance(expectedDistance)
        .serverGroupId(TestConstants.KITPVP_ID)
        .serverNetworkId(TestConstants.NETWORK_ID);
    Mockito.verify(playerClient.getAccusationsApi()).createWatchRequest(expected);
  }

  private void onCommand(String... args) {
    String[] argsIncludingBaseCommand = new String[2 + args.length];
    argsIncludingBaseCommand[0] = "reports";
    argsIncludingBaseCommand[1] = "create";
    System.arraycopy(args, 0, argsIncludingBaseCommand, 2, argsIncludingBaseCommand.length - 2);
    new CmdMMC(config).onCommand(sender, argsIncludingBaseCommand);
  }
}
