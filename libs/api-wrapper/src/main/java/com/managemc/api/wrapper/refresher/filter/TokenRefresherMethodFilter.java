package com.managemc.api.wrapper.refresher.filter;

import javassist.util.proxy.MethodFilter;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Determines which Java client methods not to perform proxying on in
 * order to prevent stack overflows.
 */
public class TokenRefresherMethodFilter implements MethodFilter {

  private static final String[] METHODS_TO_IGNORE = new String[]{
      "generateInternalToken",
      "generateExternalToken",
      "getApiClient",
      "setApiClient",
      "finalize"
  };

  @Override
  public boolean isHandled(Method method) {
    return !Arrays.asList(METHODS_TO_IGNORE).contains(method.getName()) &&
        !method.getName().endsWith("WithHttpInfo");
  }
}
