package com.managemc.importer.reader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.managemc.importer.reader.model.VanillaBan;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class VanillaBanReader implements PunishmentReader<VanillaBan> {

  private static final GsonBuilder BUILDER = new GsonBuilder();
  static final String BANNED_PLAYERS_FILENAME = "banned-players.json";

  @Override
  public List<VanillaBan> read() throws Exception {
    File file = new File(BANNED_PLAYERS_FILENAME);
    if (!file.exists()) {
      return Collections.emptyList();
    }

    String json = FileUtils.readFileToString(file, "UTF-8");

    Gson gson = BUILDER.create();
    return Arrays.asList(gson.fromJson(json, VanillaBan[].class));
  }
}
