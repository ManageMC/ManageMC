package com.managemc.plugins.config.helper;

import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class InputStreamReader {

  @SneakyThrows
  public static String readInputStream(InputStream inputStream) {
    try (BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
      return reader.lines().collect(Collectors.joining("\n")) + "\n";
    }
  }
}
