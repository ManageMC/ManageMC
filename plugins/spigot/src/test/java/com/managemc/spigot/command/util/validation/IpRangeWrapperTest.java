package com.managemc.spigot.command.util.validation;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IpRangeWrapperTest {

  private static final IpV4Wrapper MIN = new IpV4Wrapper("1.1.1.1");
  private static final IpV4Wrapper MAX = new IpV4Wrapper("2.2.2.2");
  private static final IpRangeWrapper RANGE_IP = new IpRangeWrapper(MIN, MAX);

  @Test
  public void coveredExamples() {
    assertTrue(RANGE_IP.covers(new IpV4Wrapper("1.1.1.1")));
    assertTrue(RANGE_IP.covers(new IpV4Wrapper("2.2.2.2")));
    assertTrue(RANGE_IP.covers(new IpV4Wrapper("1.2.3.4")));
    assertTrue(RANGE_IP.covers(new IpV4Wrapper("1.255.0.0")));
    assertTrue(RANGE_IP.covers(new IpV4Wrapper("1.1.255.0")));
    assertTrue(RANGE_IP.covers(new IpV4Wrapper("1.1.1.255")));
    assertTrue(RANGE_IP.covers(new IpV4Wrapper("2.0.0.0")));
  }

  @Test
  public void nonCoveredExamples() {
    assertFalse(RANGE_IP.covers(new IpV4Wrapper("1.1.1.0")));
    assertFalse(RANGE_IP.covers(new IpV4Wrapper("2.2.2.3")));
    assertFalse(RANGE_IP.covers(new IpV4Wrapper("0.0.0.0")));
    assertFalse(RANGE_IP.covers(new IpV4Wrapper("0.255.0.0")));
    assertFalse(RANGE_IP.covers(new IpV4Wrapper("1.0.255.0")));
    assertFalse(RANGE_IP.covers(new IpV4Wrapper("1.1.0.255")));
    assertFalse(RANGE_IP.covers(new IpV4Wrapper("255.255.255.255")));
  }
}
