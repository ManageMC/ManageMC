package com.managemc.spigot.command.util.validation;

import com.managemc.plugins.util.RegexConstants;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

public class IpV4Wrapper {

  private static final int MAX_VALUE = 255;

  private final String address;
  private long numericalValue;

  public IpV4Wrapper(@Nonnull String address) {
    this.address = address;
  }

  public boolean isValid() {
    if (!Pattern.compile(RegexConstants.IP_REGEX).matcher(address).matches()) {
      return false;
    }

    for (String segment : address.split("\\.")) {
      if (invalidNumber(segment)) {
        return false;
      }
    }

    return true;
  }

  public long toComparableNumericalValue() {
    if (numericalValue > 0) {
      return numericalValue;
    }
    StringBuilder value = new StringBuilder();
    for (String segment : address.split("\\.")) {
      value.append(appendZerosIfNeeded(segment));
    }
    numericalValue = Long.parseLong(value.toString());
    return numericalValue;
  }

  private boolean invalidNumber(String number) {
    int value = parseInt(number);
    if (value > MAX_VALUE) {
      return true;
    }
    return !String.valueOf(value).equals(number);
  }

  private int parseInt(String segment) {
    try {
      return Integer.parseInt(segment);
    } catch (NumberFormatException e) {
      return MAX_VALUE + 1;
    }
  }

  private String appendZerosIfNeeded(String segment) {
    switch (segment.length()) {
      case 3:
        return segment;
      case 2:
        return "0" + segment;
      case 1:
        return "00" + segment;
      default:
        throw new IllegalArgumentException("Invalid IP address segment " + segment);
    }
  }
}
