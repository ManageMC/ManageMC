package com.managemc.plugins.config;

import java.util.Map;

public interface LocalConfigLoader {

  Map<String, Object> load();
}
