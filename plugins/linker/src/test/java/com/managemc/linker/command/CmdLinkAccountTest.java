package com.managemc.linker.command;

import com.managemc.api.wrapper.ClientProvider;
import com.managemc.linker.config.AccountLinkerConfig;
import com.managemc.linker.service.AccountLinkingService;
import com.managemc.linker.testutil.AccountLinkerConfigTest;
import com.managemc.plugins.logging.BukkitLogging;
import org.awaitility.Awaitility;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RunWith(MockitoJUnitRunner.class)
public class CmdLinkAccountTest {

  private static final AccountLinkerConfig CONFIG = new AccountLinkerConfigTest();

  private static final String UNLINKED_UUID = "98b7a77b-cd65-4f72-ad91-a6c399f1921e";
  private static final String HCLEWK_TOKEN = "1_ghj567";
  private static final String BENNEY_TOKEN = "4_poi765";
  private static final String UNVERIFIED_TOKEN = "6_abc123";
  private static final String BENNEY_UUID = "132a6196-f571-4ec4-bf08-486037cafa96";

  private CmdLinkAccount command;

  @Mock
  private Player randoPlayer;
  @Mock
  private Player unlinkedPlayer;

  @Before
  public void setup() {
    command = new CmdLinkAccount(Mockito.mock(BukkitLogging.class), CONFIG.getAccountLinkingService());

    Mockito.when(randoPlayer.getUniqueId()).thenReturn(UUID.fromString(BENNEY_UUID));
    Mockito.when(unlinkedPlayer.getUniqueId()).thenReturn(UUID.fromString(UNLINKED_UUID));
  }

  @Test
  public void notEnoughArgs() {
    Assert.assertFalse(onCommand(randoPlayer));
    Mockito.verify(randoPlayer).sendMessage(CmdLinkAccount.WRONG_NUM_ARGS);
  }

  @Test
  public void tooManyArgs() {
    Assert.assertFalse(onCommand(randoPlayer, "a", "b"));
    Mockito.verify(randoPlayer).sendMessage(CmdLinkAccount.WRONG_NUM_ARGS);
  }

  @Test
  public void wrongSenderType() {
    ConsoleCommandSender nonPlayerSender = Mockito.mock(ConsoleCommandSender.class);
    Assert.assertTrue(onCommand(nonPlayerSender, "meh"));
    Mockito.verify(nonPlayerSender).sendMessage(CmdLinkAccount.WRONG_CMD_MEDIUM);
  }

  @Test
  public void wrongToken() {
    awaitWebServiceResponse(() -> onCommand(randoPlayer, "wrong"));
    Mockito.verify(randoPlayer).sendMessage(AccountLinkingService.WRONG_TOKEN_MSG);
  }

  @Test
  public void alreadyLinked() {
    awaitWebServiceResponse(() -> onCommand(randoPlayer, HCLEWK_TOKEN));
    Mockito.verify(randoPlayer).sendMessage(AccountLinkingService.ALREADY_LINKED_MSG);
  }

  @Test
  public void unverifiedEmail() {
    awaitWebServiceResponse(() -> onCommand(unlinkedPlayer, UNVERIFIED_TOKEN));
    Mockito.verify(unlinkedPlayer).sendMessage(AccountLinkingService.UNVERIFIED_EMAIL_MSG);
  }

  @Test
  public void success() {
    awaitWebServiceResponse(() -> onCommand(unlinkedPlayer, BENNEY_TOKEN));
    Mockito.verify(unlinkedPlayer).sendMessage(AccountLinkingService.SUCCESS_MSG);
  }


  private boolean onCommand(CommandSender sender, String... args) {
    return command.onCommand(sender, Mockito.mock(Command.class), "", args);
  }

  private void awaitWebServiceResponse(VoidFunctionPossiblyThrowingException function) {
    long completedRequests = ClientProvider.COMPLETED_REQUESTS.get();
    try {
      function.apply();
    } catch (Exception e) {
      e.printStackTrace();
    }
    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> ClientProvider.COMPLETED_REQUESTS.get() > completedRequests);
  }

  @FunctionalInterface
  private interface VoidFunctionPossiblyThrowingException {
    void apply() throws Exception;
  }
}
