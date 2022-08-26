package com.managemc.spigot.service;

import com.managemc.api.ApiException;
import com.managemc.spigot.config.SpigotPluginConfig;
import org.openapitools.client.model.ServerJoinEventInput;
import org.openapitools.client.model.ServerJoinEventResponse;
import org.openapitools.client.model.ServerLeaveEventInput;

import java.net.InetAddress;
import java.util.UUID;

public class LoginLogoutService {

  private final SpigotPluginConfig config;

  public LoginLogoutService(SpigotPluginConfig config) {
    this.config = config;
  }

  public ServerJoinEventResponse onLogin(String username, UUID uuid, InetAddress ipAddress) throws ApiException {
    ServerJoinEventInput input = new ServerJoinEventInput()
        .timeMillis(now())
        .ipAddress(ipAddress.getHostAddress())
        .username(username)
        .uuid(uuid)
        .serverId(config.getLocalConfig().getDynamicComponents().getServerMetadata().getServerId());
    return config.getClientProvider().externalServer().getServerTrafficApi().recordServerJoinEvent(input);
  }

  public void onLogout(UUID uuid) throws ApiException {
    ServerLeaveEventInput input = new ServerLeaveEventInput()
        .playerId(config.getPlayerData().getPlayerId(uuid))
        .timeMillis(now())
        .serverId(config.getLocalConfig().getDynamicComponents().getServerMetadata().getServerId());
    config.getClientProvider().externalServer().getServerTrafficApi().recordServerLeaveEvent(input);
  }

  private long now() {
    return System.currentTimeMillis();
  }
}
