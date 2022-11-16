package com.managemc.plugins.config;

import com.managemc.plugins.config.helper.InputStreamReader;

import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class FlexibleLocalConfigLoader implements LocalConfigLoader {

  private final String[] keys;
  private final SystemPropertiesBasedLocalConfigLoader sysPropsLoader;
  private final EnvironmentBasedLocalConfigLoader envLoader;
  private final FileBasedLocalConfigLoader fileLoader;

  public FlexibleLocalConfigLoader(
      String[] keys,
      File dataFolder,
      String fileName,
      InputStream defaultContents
  ) {
    this(keys, dataFolder, fileName, InputStreamReader.readInputStream(defaultContents));
  }

  public FlexibleLocalConfigLoader(
      String[] keys,
      File dataFolder,
      String fileName,
      String defaultContents
  ) {
    this.keys = keys;
    this.sysPropsLoader = new SystemPropertiesBasedLocalConfigLoader(keys);
    this.envLoader = new EnvironmentBasedLocalConfigLoader(keys);
    this.fileLoader = new FileBasedLocalConfigLoader(dataFolder, fileName, defaultContents);
  }

  FlexibleLocalConfigLoader(
      String[] keys,
      SystemPropertiesBasedLocalConfigLoader sysPropsLoader,
      EnvironmentBasedLocalConfigLoader envLoader,
      FileBasedLocalConfigLoader fileLoader
  ) {
    this.keys = keys;
    this.sysPropsLoader = sysPropsLoader;
    this.envLoader = envLoader;
    this.fileLoader = fileLoader;
  }

  @Override
  public Map<String, Object> load() {
    Map<String, Object> configMap = sysPropsLoader.load();

    if (Stream.of(keys).map(configMap::get).noneMatch(Objects::isNull)) {
      return configMap;
    }

    envLoader.load().forEach(configMap::putIfAbsent);

    if (Stream.of(keys).map(configMap::get).noneMatch(Objects::isNull)) {
      return configMap;
    }

    fileLoader.load().forEach(configMap::putIfAbsent);

    return configMap;
  }
}
