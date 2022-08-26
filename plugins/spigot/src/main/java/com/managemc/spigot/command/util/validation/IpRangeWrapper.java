package com.managemc.spigot.command.util.validation;

import javax.annotation.Nonnull;

public class IpRangeWrapper {

  private final IpV4Wrapper minAddress;
  private final IpV4Wrapper maxAddress;

  public IpRangeWrapper(@Nonnull IpV4Wrapper minAddress, @Nonnull IpV4Wrapper maxAddress) {
    this.minAddress = minAddress;
    this.maxAddress = maxAddress;
  }

  public boolean covers(@Nonnull IpV4Wrapper ipAddress) {
    return ipAddress.toComparableNumericalValue() >= minAddress.toComparableNumericalValue() &&
        ipAddress.toComparableNumericalValue() <= maxAddress.toComparableNumericalValue();
  }
}
