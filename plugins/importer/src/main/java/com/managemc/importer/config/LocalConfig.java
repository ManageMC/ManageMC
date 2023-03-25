package com.managemc.importer.config;

import com.managemc.api.wrapper.model.Keys;
import com.managemc.importer.environment.Environment;
import lombok.Getter;

import java.util.Map;
import java.util.Optional;

public class LocalConfig {

  public static final String PUBLIC_KEY_KEY = "MMC_PUBLIC_KEY";
  public static final String PRIVATE_KEY_KEY = "MMC_PRIVATE_KEY";
  public static final String ENVIRONMENT_KEY = "MMC_ENVIRONMENT";

  @Getter
  private final Keys keys;
  @Getter
  private final Environment environment;

  public LocalConfig(Map<String, Object> configMap) {
    this.keys = new Keys(
        fetchKey(configMap, PUBLIC_KEY_KEY),
        fetchKey(configMap, PRIVATE_KEY_KEY)
    );
    String envString = Optional.ofNullable(configMap.get(ENVIRONMENT_KEY))
        .map(Object::toString)
        .orElse(Environment.PRODUCTION.toString());
    this.environment = Environment.valueOf(envString.toUpperCase());
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
}
