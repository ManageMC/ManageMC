package com.managemc.spigot.command.util.validation;

import javax.annotation.Nonnull;

public class RangeIpValidator {

  public boolean isValid(@Nonnull String rangeIp) {
    String[] twoIps = rangeIp.split("-");
    if (twoIps.length != 2) {
      return false;
    }

    IpV4Wrapper minAddress = new IpV4Wrapper(twoIps[0]);
    IpV4Wrapper maxAddress = new IpV4Wrapper(twoIps[1]);

    return minAddress.isValid() &&
        maxAddress.isValid() &&
        minAddress.toComparableNumericalValue() < maxAddress.toComparableNumericalValue();
  }
}
