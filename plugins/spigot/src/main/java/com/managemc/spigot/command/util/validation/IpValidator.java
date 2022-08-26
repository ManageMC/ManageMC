package com.managemc.spigot.command.util.validation;

abstract class IpValidator {

  private static final int MAX_VALUE = 255;

  protected boolean invalidNumber(String number) {
    int value = parseInt(number);
    if (value > MAX_VALUE) {
      return true;
    }
    return !String.valueOf(value).equals(number);
  }

  protected int parseInt(String segment) {
    try {
      return Integer.parseInt(segment);
    } catch (NumberFormatException e) {
      return MAX_VALUE + 1;
    }
  }
}
