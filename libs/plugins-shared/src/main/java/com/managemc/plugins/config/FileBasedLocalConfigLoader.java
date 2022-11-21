package com.managemc.plugins.config;

import com.managemc.plugins.config.helper.InputStreamReader;
import lombok.SneakyThrows;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class FileBasedLocalConfigLoader implements LocalConfigLoader {

  public static String INVALID_YAML_MESSAGE = "Invalid YAML detected at %s. Try validating it here: http://www.yamllint.com/";

  private final String fileName;
  private final File dataFolder;
  private final String defaultContents;

  public FileBasedLocalConfigLoader(File dataFolder, String fileName, InputStream defaultContents) {
    this(dataFolder, fileName, InputStreamReader.readInputStream(defaultContents));
  }

  public FileBasedLocalConfigLoader(File dataFolder, String fileName, String defaultContents) {
    this.dataFolder = dataFolder;
    this.fileName = fileName;
    this.defaultContents = defaultContents;
  }

  @Override
  public Map<String, Object> load() {
    createConfigFileIfNecessary();
    Yaml yaml = new Yaml();

    try (InputStream inputStream = Files.newInputStream(Paths.get(configFilePath()))) {
      return yaml.load(inputStream);
    } catch (IOException e) {
      String message = String.format(INVALID_YAML_MESSAGE, configFilePath());
      throw new RuntimeException(message);
    }
  }

  private String configFilePath() {
    return dataFolder + "/" + fileName;
  }

  @SneakyThrows
  private void createConfigFileIfNecessary() {
    dataFolder.mkdir();
    File configFile = new File(configFilePath());
    if (!configFile.exists()) {
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
        writer.write(defaultContents);
      }
    }
  }
}
