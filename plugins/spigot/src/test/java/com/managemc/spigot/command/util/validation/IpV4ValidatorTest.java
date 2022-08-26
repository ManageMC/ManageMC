package com.managemc.spigot.command.util.validation;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IpV4ValidatorTest {

  private static final IpV4Validator VALIDATOR = new IpV4Validator();

  @Test
  public void emptyString() {
    assertFalse(VALIDATOR.isValid(""));
  }

  @Test
  public void wrongNumberOfSegments() {
    assertFalse(VALIDATOR.isValid("1.1.1"));
  }

  @Test
  public void wrongNumberOfSegmentsTrailingPeriod() {
    assertFalse(VALIDATOR.isValid("1.1.1."));
  }

  @Test
  public void trailingPeriod() {
    assertFalse(VALIDATOR.isValid("1.1.1.1."));
  }

  @Test
  public void emptySegment() {
    assertFalse(VALIDATOR.isValid("1.1..1"));
  }

  @Test
  public void leadingZero() {
    assertFalse(VALIDATOR.isValid("01.1.1.1"));
  }

  @Test
  public void nonNumericCharacters() {
    assertFalse(VALIDATOR.isValid("a.1.1.1"));
  }

  @Test
  public void negativeSign() {
    assertFalse(VALIDATOR.isValid("-1.1.1.1"));
  }

  @Test
  public void longboye() {
    assertFalse(VALIDATOR.isValid("127.0.0.21263581726351827365127635"));
  }

  @Test
  public void ipv6() {
    assertFalse(VALIDATOR.isValid("2001:0db8:85a3:0000:0000:8a2e:0370:7334"));
  }

  @Test
  public void severalExamplesOfPerfectlyReasonableIpAddresses() {
    assertTrue(VALIDATOR.isValid("127.0.0.1"));
    assertTrue(VALIDATOR.isValid("1.1.1.1"));
    assertTrue(VALIDATOR.isValid("255.255.255.255"));
  }
}
