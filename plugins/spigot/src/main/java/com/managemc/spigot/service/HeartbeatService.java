package com.managemc.spigot.service;

import com.managemc.api.ApiException;
import com.managemc.spigot.config.SpigotPluginConfig;
import com.managemc.spigot.config.model.LocalConfig;
import com.managemc.spigot.config.model.ServerMetadata;
import com.managemc.spigot.task.PeriodicHeartbeatSender;
import org.openapitools.client.model.*;

public class HeartbeatService {

  public static final String OUT_OF_DATE_MESSAGE = "Your ManageMC plugin is out of date and will eventually be unsupported. Please update at your convenience to avoid unexpected loss of service.";
  public static final String UNSUPPORTED_MESSAGE = "Your ManageMC version is no longer supported. Please update to resume service.";

  private final SpigotPluginConfig config;

  public HeartbeatService(SpigotPluginConfig config) {
    this.config = config;
  }

  public void emitInitialHeartbeat() throws ApiException {
    EmitInitialHeartbeatInput input = new EmitInitialHeartbeatInput()
        .serverName(config.getLocalConfig().getServerName())
        .version(config.getLocalConfig().getVersion());
    EmitInitialHeartbeatResponse response = config.getClientProvider().externalServer().getHeartbeatsApi()
        .emitInitialHeartbeat(input);
    processResponse(response.getVersionStatus());
    ServerMetadata serverMetadata = ServerMetadata.builder()
        .serverGroupId(config.getClientProvider().externalServer().getAuthMetadata().getServerGroupId())
        .serverGroup(config.getLocalConfig().getServerGroup())
        .serverId(response.getServerId())
        .build();
    config.getLocalConfig().setDynamicComponents(new LocalConfig.DynamicComponents(serverMetadata));
  }

  public void emitHeartbeat() throws ApiException {
    EmitHeartbeatInput input = new EmitHeartbeatInput()
        .version(config.getLocalConfig().getVersion())
        .serverId(config.getLocalConfig().getDynamicComponents().getServerMetadata().getServerId())
        .playerCount(config.getBukkitWrapper().getOnlinePlayers().size())
        .playerCountCollectedAtMillis(periodicHeartbeatTime());
    EmitHeartbeatResponse response = config.getClientProvider().externalServer().getHeartbeatsApi().emitHeartbeat(input);
    processResponse(response.getVersionStatus());
  }

  /**
   * Only emits final heartbeat if serverMetadata is assigned. If it were not
   * assigned, then that would mean we never successfully sent the initial heartbeat,
   * making it meaningless to send a final one.
   */
  public void emitFinalHeartbeat() throws ApiException {
    if (config.getLocalConfig().getDynamicComponents() != null) {
      EmitFinalHeartbeatInput input = new EmitFinalHeartbeatInput()
          .serverId(config.getLocalConfig().getDynamicComponents().getServerMetadata().getServerId());
      config.getClientProvider().externalServer().getHeartbeatsApi().emitFinalHeartbeat(input);
    }
  }

  long periodicHeartbeatTime() {
    long now = currentTime();
    return now % PeriodicHeartbeatSender.HEARTBEAT_PERIOD_MILLIS == 0
        ? now
        : now - (now % PeriodicHeartbeatSender.HEARTBEAT_PERIOD_MILLIS);
  }

  long currentTime() {
    return System.currentTimeMillis();
  }

  void processResponse(PluginVersionStatus status) {
    switch (status) {
      case OK:
        break;
      case DEPRECATED:
        config.getLogging().logWarning(OUT_OF_DATE_MESSAGE);
        break;
      case NOT_SUPPORTED:
        config.getLogging().logSevere(UNSUPPORTED_MESSAGE);
        config.getBukkitWrapper().shutdown();
        break;
      default:
        throw new RuntimeException("Unexpected PluginVersionStatus " + status);
    }
  }
}
