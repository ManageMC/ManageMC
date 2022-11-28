package com.managemc.spigot.service;

import com.managemc.api.ApiException;
import com.managemc.spigot.config.SpigotPluginConfig;
import com.managemc.spigot.config.model.LocalConfig;
import com.managemc.spigot.config.model.ServerMetadata;
import com.managemc.spigot.task.PeriodicHeartbeatSender;
import org.openapitools.client.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HeartbeatService {

  public static final String OUT_OF_DATE_MESSAGE = "Your ManageMC plugin is out of date and will eventually be unsupported. Please update at your convenience to avoid unexpected loss of service.";
  public static final String UNSUPPORTED_MESSAGE = "Your ManageMC version is no longer supported. Please update to resume service.";
  public static final String CAUGHT_UP_MESSAGE = "Heartbeat succeeded. Player count data is up to date now.";

  private final SpigotPluginConfig config;
  private final Map<PlayerCountSnapshot, Boolean> playerCounts = new ConcurrentHashMap<>();

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
    List<PlayerCountSnapshot> toSubmit = new ArrayList<>(playerCounts.keySet());
    playerCounts.clear();

    PlayerCountSnapshot currentCount = new PlayerCountSnapshot()
        .count(config.getBukkitWrapper().getOnlinePlayers().size())
        .timestamp(periodicHeartbeatTime());

    toSubmit.add(currentCount);

    EmitHeartbeatInput input = new EmitHeartbeatInput()
        .version(config.getLocalConfig().getVersion())
        .serverId(config.getLocalConfig().getDynamicComponents().getServerMetadata().getServerId())
        .playerCountData(toSubmit);

    try {
      EmitHeartbeatResponse response = config.getClientProvider().externalServer().getHeartbeatsApi().emitHeartbeat(input);
      processResponse(response.getVersionStatus());
      if (toSubmit.size() > 1) {
        config.getLogging().logInfo(CAUGHT_UP_MESSAGE);
      }
    } catch (Exception e) {
      toSubmit.forEach(snapshot -> playerCounts.put(snapshot, true));
      throw e;
    }
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

  /**
   * Heartbeats MUST be sent exactly 0, 20, or 40 milliseconds after the minute. Our
   * heartbeat sender runs exactly at these times in a separate thread that shouldn't
   * suffer from lag associated with Minecraft or Spigot plugins, but just in case
   * there is a slight delay, we round the current time to the most recent 0/20/40
   * second mark here.
   */
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
