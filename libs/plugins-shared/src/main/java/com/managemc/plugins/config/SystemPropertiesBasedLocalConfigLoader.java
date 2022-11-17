package com.managemc.plugins.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SystemPropertiesBasedLocalConfigLoader implements LocalConfigLoader {

  private final String[] keys;

  public SystemPropertiesBasedLocalConfigLoader(String[] keys) {
    this.keys = keys;
  }

  @Override
  public Map<String, Object> load() {
    Map<String, Object> properties = new HashMap<>();

    for (String key : keys) {
      Optional.ofNullable(getSystemProperty(key))
          .ifPresent(value -> properties.put(key, value));
    }

    return properties;
  }

  String getSystemProperty(String key) {
    return System.getProperty(key);
  }
}
