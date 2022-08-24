package com.managemc.spigot.state;

import com.managemc.api.ApiException;
import com.managemc.api.wrapper.ClientProvider;
import lombok.AccessLevel;
import lombok.Getter;
import org.openapitools.client.model.ServerGroupAbridged;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerGroupsData {

  private final Map<Long, ServerGroupAbridged> serverGroups = new ConcurrentHashMap<>();
  private final ClientProvider clientProvider;

  public ServerGroupsData(ClientProvider clientProvider) {
    this.clientProvider = clientProvider;
  }

  public ServerGroupAbridged getServerGroup(long id) {
    ServerGroupAbridged group = serverGroups.get(id);
    if (group == null) {
      needsRefresh = true;
    }
    return group;
  }

  @Getter(value = AccessLevel.PROTECTED)
  private boolean needsRefresh = true;

  public void refreshBlocking() throws ApiException {
    clientProvider.externalServer().getServerGroupsApi().fetchServerGroups(networkId())
        .forEach(group -> serverGroups.put(group.getId(), group));
    needsRefresh = false;
  }

  public void refreshBlockingIfNeeded() throws ApiException {
    if (needsRefresh) {
      refreshBlocking();
    }
  }

  /**
   * May produce NPE if called immediately as class is instantiated.
   */
  private long networkId() {
    return clientProvider.externalServer().getAuthMetadata().getServerNetworkId();
  }
}
