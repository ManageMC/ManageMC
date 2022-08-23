package com.managemc.plugins.util;

import java.util.UUID;

public class UuidHyphenator {

  /*
      shamelessly copypasta'd from Stack Overflow
      https://stackoverflow.com/questions/18986712/creating-a-uuid-from-a-string-with-no-dashes
   */
  private static final String REGEX = "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)";

  /*
      Some of our customers store UUIDs without their original hyphens, but we have elected to,
      which poses a problem for onboarding old customer punishment data.
  */
  public static UUID toUUID(String stringPossiblyWithoutHyphens) {
    if (stringPossiblyWithoutHyphens == null) {
      throw new IllegalArgumentException();
    }
    if (stringPossiblyWithoutHyphens.contains("-")) {
      return UUID.fromString(stringPossiblyWithoutHyphens);
    }
    String hyphenatedString = stringPossiblyWithoutHyphens
        .replaceFirst(REGEX, "$1-$2-$3-$4-$5");
    return UUID.fromString(hyphenatedString);
  }
}
