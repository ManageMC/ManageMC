package com.managemc.plugins.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EnvironmentBasedLocalConfigLoader implements LocalConfigLoader {

  private final String[] keys;

  public EnvironmentBasedLocalConfigLoader(String[] keys) {
    this.keys = keys;
  }

  @Override
  public Map<String, Object> load() {
    Map<String, Object> properties = new HashMap<>();

    for (String key : keys) {
      Optional.ofNullable(getEnvironmentVariable(key))
          .ifPresent(value -> properties.put(key, value));
    }

    return properties;
  }

  String getEnvironmentVariable(String key) {
    return System.getenv(key);
  }
}
