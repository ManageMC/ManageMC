package com.managemc.spigot.util;

import org.ocpsoft.prettytime.PrettyTime;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeHelper {

  public static long ONE_SECOND = 1000;
  public static long ONE_MINUTE = ONE_SECOND * 60;
  public static long ONE_HOUR = ONE_MINUTE * 60;
  public static long ONE_DAY = ONE_HOUR * 24;

  // interactive testing https://javadevtools.com/simpledateformat
  private static final String PATTERN = "yyyy-MM-dd -- hh:mm:ss a z";
  private static final SimpleDateFormat FORMATTER = new SimpleDateFormat(PATTERN);
  private static final PrettyTime RELATIVE_FORMATTER = new PrettyTime();

  public static String formatTimestamp(Long timeMillis) {
    return formatTimestamp(timeMillis, null);
  }

  public static String formatTimestamp(Long timeMillis, String ifNull) {
    if (timeMillis == null) {
      return ifNull;
    }
    return FORMATTER.format(new Date(timeMillis));
  }

  public static String relativeTime(long epochMillisFromNow) {
    return RELATIVE_FORMATTER.format(new Date(epochMillisFromNow));
  }
}
