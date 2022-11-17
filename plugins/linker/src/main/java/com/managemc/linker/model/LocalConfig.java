package com.managemc.linker.model;

import com.managemc.api.wrapper.model.Keys;
import lombok.Getter;

import java.util.Map;
import java.util.Optional;

public class LocalConfig {

  public static final String PUBLIC_KEY_KEY = "MMC_PUBLIC_KEY";
  public static final String PRIVATE_KEY_KEY = "MMC_PRIVATE_KEY";

  @Getter
  private final Keys keys;

  public LocalConfig(Map<String, Object> configMap) {
    this.keys = new Keys(
        fetchKey(configMap, PUBLIC_KEY_KEY),
        fetchKey(configMap, PRIVATE_KEY_KEY)
    );
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
