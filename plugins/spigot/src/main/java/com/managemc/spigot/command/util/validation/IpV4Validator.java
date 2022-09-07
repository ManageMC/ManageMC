package com.managemc.spigot.command.util.validation;

import javax.annotation.Nonnull;

public class IpV4Validator {

  public boolean isValid(@Nonnull String ipAddress) {
    return new IpV4Wrapper(ipAddress).isValid();
  }
}
