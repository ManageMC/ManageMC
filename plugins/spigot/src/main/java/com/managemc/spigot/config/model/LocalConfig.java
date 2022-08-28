package com.managemc.spigot.config.model;

import com.managemc.api.wrapper.model.Keys;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Optional;

public class LocalConfig {

  public static final String PUBLIC_KEY_KEY = "public_key";
  public static final String PRIVATE_KEY_KEY = "private_key";
  public static final String SERVER_GROUP_KEY = "group";
  public static final String SERVER_NAME_KEY = "name";
  public static final String DEFAULT_SERVER_NAME = "Unnamed Server";

  @Getter
  private final Keys keys;
  @Getter
  private final String serverGroup;
  @Getter
  private final String serverName;
  @Getter
  @Setter
  private String version;
  @Getter
  @Setter
  private DynamicComponents dynamicComponents;

  public LocalConfig(Map<String, Object> configMap, String version) {
    this.keys = new Keys(
        fetchKey(configMap, PUBLIC_KEY_KEY),
        fetchKey(configMap, PRIVATE_KEY_KEY)
    );
    this.serverGroup = fetchKey(configMap, SERVER_GROUP_KEY);
    this.serverName = Optional
        .ofNullable(configMap.get(SERVER_NAME_KEY))
        .map(Object::toString)
        .orElse(DEFAULT_SERVER_NAME);
    this.version = version;
  }

  private String fetchKey(Map<String, Object> configMap, String key) {
    return Optional
        .ofNullable(configMap.get(key))
        .map(Object::toString)
        .orElseThrow(() -> new IncompleteConfigException(key));
  }

  public static class IncompleteConfigException extends RuntimeException {
    public IncompleteConfigException(String key) {
      super("Missing required entry from local config file: " + key);
    }
  }

  // portions of the "local config" that are assigned after the fact, i.e. not in constructor
  // currently only metadata fetched from the initial heartbeat is stored here
  // the test suite tends to mock this data
  public static class DynamicComponents {
    @Getter
    private final ServerMetadata serverMetadata;

    public DynamicComponents(ServerMetadata serverMetadata) {
      this.serverMetadata = serverMetadata;
    }
  }
}
