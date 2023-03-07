package com.managemc.spigot.service;

import com.managemc.api.ApiException;
import com.managemc.api.wrapper.model.metadata.ExternalServerAuthMetadata;
import com.managemc.plugins.command.AbortCommand;
import com.managemc.spigot.config.SpigotPluginConfig;
import org.bukkit.entity.Player;
import org.openapitools.client.model.CreateReportInput;

import java.util.UUID;

public class ReportsService {

  private final SpigotPluginConfig config;

  public ReportsService(SpigotPluginConfig config) {
    this.config = config;
  }

  public void reportOnlinePlayer(Player sender, Player accused, String reason) throws ApiException {
    int distanceBetween = (int) accused.getLocation().distance(sender.getLocation());
    reportPlayer(sender, accused.getUniqueId(), reason, distanceBetween);
  }

  public void reportOfflinePlayer(Player sender, UUID accusedId, String reason) throws ApiException {
    reportPlayer(sender, accusedId, reason, null);
  }

  private void reportPlayer(Player sender, UUID accusedId, String reason, Integer distanceBetween) throws ApiException {
    ExternalServerAuthMetadata authMetadata = config.getClientProvider().externalServer().getAuthMetadata();

    CreateReportInput input = new CreateReportInput()
        .accuseeUuid(accusedId)
        .inGameDistance(distanceBetween)
        .serverGroupId(authMetadata.getServerGroupId())
        .serverNetworkId(authMetadata.getServerNetworkId())
        .summary(reason)
        .status(CreateReportInput.StatusEnum.OPEN);

    try {
      config.getClientProvider().player(sender.getUniqueId()).getReportsApi()
          .createReport(input);
    } catch (ApiException e) {
      if (e.getCode() == 422 && e.getResponseBody().contains("Accusee not found")) {
        throw AbortCommand.withoutUsageMessage(WatchlistService.PLAYER_NOT_FOUND);
      }
      throw e;
    }
  }
}
