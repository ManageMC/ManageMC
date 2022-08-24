package com.managemc.importer.reader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.managemc.importer.reader.model.VanillaIpBan;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class VanillaIpBanReader implements PunishmentReader<VanillaIpBan> {

  private static final GsonBuilder BUILDER = new GsonBuilder();
  static final String BANNED_IPS_FILENAME = "banned-ips.json";

  @Override
  public List<VanillaIpBan> read() throws Exception {
    File file = new File(BANNED_IPS_FILENAME);
    if (!file.exists()) {
      return Collections.emptyList();
    }

    String json = FileUtils.readFileToString(file, "UTF-8");

    Gson gson = BUILDER.create();
    return Arrays.asList(gson.fromJson(json, VanillaIpBan[].class));
  }
}
