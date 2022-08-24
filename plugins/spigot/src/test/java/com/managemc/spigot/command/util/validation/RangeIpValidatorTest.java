package com.managemc.spigot.command.util.validation;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RangeIpValidatorTest {

  private static final RangeIpValidator VALIDATOR = new RangeIpValidator();

  @Test
  public void normalIp() {
    assertFalse(VALIDATOR.isValid("127.0.0.1"));
  }

  @Test
  public void oneIpIsInvalid() {
    assertFalse(VALIDATOR.isValid("127.01.0.0-155.5.5.5"));
  }

  @Test
  public void noPeriods() {
    assertFalse(VALIDATOR.isValid("127"));
  }

  @Test
  public void justAHyphen() {
    assertFalse(VALIDATOR.isValid("-"));
  }

  @Test
  public void emptyString() {
    assertFalse(VALIDATOR.isValid(""));
  }

  @Test
  public void longboye() {
    assertFalse(VALIDATOR.isValid("0.0.0.0-127.0.0.21263581726351827365127635"));
  }

  @Test
  public void moreThenOneHyphen() {
    assertFalse(VALIDATOR.isValid("0.0.0.0-1.0.0.0-2.0.0.0"));
  }

  @Test
  public void ipsAreEqual() {
    assertFalse(VALIDATOR.isValid("0.0.0.0-0.0.0.0"));
  }

  @Test
  public void minGreaterThanMax() {
    assertFalse(VALIDATOR.isValid("0.0.0.2-0.0.0.1"));
  }

  @Test
  public void validExample() {
    assertTrue(VALIDATOR.isValid("255.1.1.1-255.2.0.0"));
  }
}
