package com.managemc.linker.service;

import com.managemc.api.ApiException;
import com.managemc.api.wrapper.ClientProvider;
import com.managemc.plugins.logging.BukkitLogging;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.openapitools.client.model.LinkPlayerInput;
import org.openapitools.client.model.LinkPlayerResponse;

public class AccountLinkingService {

  public static final String SUCCESS_MSG = ChatColor.GREEN + "Account linked successfully. If you need to link another Minecraft account, you will need to use a new token.";
  public static final String WRONG_TOKEN_MSG = ChatColor.RED + "Wrong account linking token";
  public static final String ALREADY_LINKED_MSG = ChatColor.RED + "A different user has already linked this account";
  public static final String UNVERIFIED_EMAIL_MSG = ChatColor.RED + "Please verify your email address first";
  public static final String UNEXPECTED_EXCEPTION = ChatColor.DARK_RED + "Unexpected plugin exception";

  private final BukkitLogging logger;
  private final ClientProvider clientProvider;

  public AccountLinkingService(BukkitLogging logger, ClientProvider clientProvider) {
    this.logger = logger;
    this.clientProvider = clientProvider;
  }

  public void linkPlayer(Player sender, String token) {
    LinkPlayerInput input = new LinkPlayerInput()
        .accountLinkingToken(token)
        .uuid(sender.getUniqueId());

    new Thread(() -> {
      try {
        LinkPlayerResponse response = clientProvider.internal().getAccountsApi().linkPlayer(input);
        sender.sendMessage(getMessageForConsumer(response));
      } catch (ApiException | RuntimeException e) {
        logger.logStackTrace(e);
        sender.sendMessage(UNEXPECTED_EXCEPTION);
      }
    }).start();
  }

  private String getMessageForConsumer(LinkPlayerResponse response) {
    switch (response.getStatus()) {
      case SUCCESS:
        return SUCCESS_MSG;
      case TOKEN_INCORRECT:
        return WRONG_TOKEN_MSG;
      case ALREADY_LINKED:
        return ALREADY_LINKED_MSG;
      case EMAIL_NOT_VERIFIED:
        return UNVERIFIED_EMAIL_MSG;
      default:
        throw new RuntimeException("Unexpected LinkPlayerResponse status " + response.getStatus());
    }
  }
}
