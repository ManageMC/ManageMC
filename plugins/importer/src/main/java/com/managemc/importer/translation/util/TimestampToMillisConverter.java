package com.managemc.importer.translation.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class TimestampToMillisConverter {

  public static long toMillis(String timestamp) throws ParseException {
    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z")
        .parse(timestamp)
        .getTime();
  }
}
