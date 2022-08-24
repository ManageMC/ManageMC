package com.managemc.importer.reader.constant;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PunishmentReaderConstants {

  public static final Set<String> VANILLA_CONSOLE_USERS = new HashSet<>(
      // Server is the default true Vanilla console user
      // Console is the one EssentialsX uses
      Arrays.asList("Server", "Console")
  );
  public static final String VANILLA_PERMBAN_EXPIRES_FIELD = "forever";
  public static final String ADVANCEDBAN_CONSOLE_USER = "CONSOLE";
}
