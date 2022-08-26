package com.managemc.spigot.config.helper;

import com.managemc.plugins.logging.BukkitLogging;

import java.io.IOException;
import java.util.Properties;

public class VersionDiscerner {

  private final BukkitLogging logging;

  public VersionDiscerner(BukkitLogging logging) {
    this.logging = logging;
  }

  public String determineVersion() throws IOException {
    try {
      final Properties properties = new Properties();
      properties.load(this.getClass().getClassLoader().getResourceAsStream(".properties"));
      return properties.getProperty("managemc-version");
    } catch (IOException e) {
      logging.logSevere("Failed to determine current ManageMC version.");
      throw e;
    }
  }
}
