package com.managemc.spigot.command.util.punishments;

import com.managemc.spigot.command.util.CommandValidationException;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DurationStringParser {

  static final long MILLIS_PER_SECOND = 1000;
  static final long MILLIS_PER_MINUTE = 60 * MILLIS_PER_SECOND;
  static final long MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE;
  static final long MILLIS_PER_DAY = 24 * MILLIS_PER_HOUR;
  private static final long MILLIS_PER_WEEK = 7 * MILLIS_PER_DAY;
  private static final long MILLIS_PER_MONTH = 30 * MILLIS_PER_DAY;
  private static final long MILLIS_PER_YEAR = 365 * MILLIS_PER_DAY;

  private static final Map<String, Long> LABELS = new HashMap<String, Long>() {
    {
      put("s", MILLIS_PER_SECOND);
      put("m", MILLIS_PER_MINUTE);
      put("h", MILLIS_PER_HOUR);
      put("d", MILLIS_PER_DAY);
      put("w", MILLIS_PER_WEEK);
      put("M", MILLIS_PER_MONTH);
      put("y", MILLIS_PER_YEAR);
    }
  };

  private static final String ALL_LABELS = "[" + String.join("", LABELS.keySet()) + "]";
  private static final String VALIDITY_REGEX = "^(\\d+" + ALL_LABELS + ")+$";

  static final String INVALID_MESSAGE = "Invalid duration \"%s\" (example: --duration=24h)";

  public long parse(@Nonnull String durationString) {
    assertValid(durationString);
    long sum = 0;
    Matcher matcher = Pattern.compile("\\d+" + ALL_LABELS).matcher(durationString);
    while (matcher.find()) {
      sum += parseSingleSegment(durationString, matcher.start(), matcher.end());
    }
    return sum;
  }

  private void assertValid(String durationString) {
    if (!durationString.matches(VALIDITY_REGEX)) {
      throw new CommandValidationException(String.format(INVALID_MESSAGE, durationString), true);
    }
  }

  private long parseSingleSegment(String durationString, int startIndex, int endIndex) {
    String numberString = durationString.substring(startIndex, endIndex - 1);
    int userInput = Integer.parseInt(numberString);
    String label = durationString.substring(endIndex - 1, endIndex);
    return userInput * LABELS.get(label);
  }
}
