package com.managemc.spigot.command.util.punishments;

import com.managemc.spigot.command.util.CommandValidationException;
import com.managemc.spigot.util.TimeHelper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

import static org.junit.Assert.assertEquals;

public class DurationStringParserTest {

  private final DurationStringParser parser = new DurationStringParser();

  @Test
  public void emptyStringInput() {
    assertThrowsException("Invalid duration \"\" (example: --duration=24h)", () -> parser.parse(""));
  }

  @Test
  public void justANumber() {
    assertThrowsException("Invalid duration \"69\" (example: --duration=24h)", () -> parser.parse("69"));
  }

  @Test
  public void justALetter() {
    assertThrowsException("Invalid duration \"h\" (example: --duration=24h)", () -> parser.parse("h"));
  }

  @Test
  public void invalidLabel() {
    assertThrowsException("Invalid duration \"1p\" (example: --duration=24h)", () -> parser.parse("1p"));
  }

  @Test
  public void twoConsecutiveLetters() {
    assertThrowsException("Invalid duration \"1hh\" (example: --duration=24h)", () -> parser.parse("1hh"));
  }

  @Test
  public void spaces() {
    assertThrowsException("Invalid duration \"1d 2h\" (example: --duration=24h)", () -> parser.parse("1d 2h"));
  }

  @Test
  public void nonAlphanumeric() {
    assertThrowsException("Invalid duration \"1h%\" (example: --duration=24h)", () -> parser.parse("1h%"));
  }

  @Test
  public void negativeCoefficient() {
    assertThrowsException("Invalid duration \"-1h\" (example: --duration=24h)", () -> parser.parse("-1h"));
  }

  @Test
  public void decimalCoefficient() {
    assertThrowsException("Invalid duration \"0.5h\" (example: --duration=24h)", () -> parser.parse("0.5h"));
  }

  @Test
  public void unsupportedCapitalLetters() {
    assertThrowsException("Invalid duration \"5H\" (example: --duration=24h)", () -> parser.parse("5H"));
  }

  @Test
  public void secondsOnly() {
    assertEquals(60 * 1000L, parser.parse("60s"));
  }

  @Test
  public void minutesOnly() {
    assertEquals(2 * 60 * 1000L, parser.parse("2m"));
  }

  @Test
  public void hoursOnly() {
    assertEquals(12 * 60 * 60 * 1000L, parser.parse("12h"));
  }

  @Test
  public void daysOnly() {
    assertEquals(10 * 24 * 60 * 60 * 1000L, parser.parse("10d"));
  }

  @Test
  public void weeksOnly() {
    assertEquals(4 * 7 * 24 * 60 * 60 * 1000L, parser.parse("4w"));
  }

  @Test
  public void monthsOnly() {
    assertEquals(3 * 30 * 24 * 60 * 60 * 1000L, parser.parse("3M"));
  }

  @Test
  public void yearsOnly() {
    assertEquals(2 * 365 * 24 * 60 * 60 * 1000L, parser.parse("2y"));
  }

  @Test
  public void combinationOfAllLabels() {
    assertEquals(36212754000L, parser.parse("1y2w10d3h5m54s1M"));
  }

  @Test
  public void repeatedLabels() {
    assertEquals(4 * 1000 + 6 * 60 * 1000L, parser.parse("1s3s1m5m"));
  }

  @Test
  public void fourSegments() {
    assertEquals(TimeHelper.ONE_DAY * 25 + TimeHelper.ONE_HOUR * 2 + TimeHelper.ONE_MINUTE * 10 + TimeHelper.ONE_SECOND * 30, parser.parse("25d2h10m30s"));
  }

  @Test
  public void fourSegmentsSingularized() {
    assertEquals(TimeHelper.ONE_DAY + TimeHelper.ONE_HOUR + TimeHelper.ONE_MINUTE + TimeHelper.ONE_SECOND, parser.parse("1d1h1m1s"));
  }

  @Test
  public void twoAdjacentSegments() {
    assertEquals(TimeHelper.ONE_DAY * 3 + TimeHelper.ONE_HOUR * 12, parser.parse("3d12h"));
  }

  @Test
  public void twoNonAdjacentSegments() {
    assertEquals(TimeHelper.ONE_DAY * 3 + TimeHelper.ONE_SECOND * 10, parser.parse("3d10s"));
  }

  @Test
  public void twoAdjacentSegmentsSingularized() {
    assertEquals(TimeHelper.ONE_DAY + TimeHelper.ONE_HOUR, parser.parse("1d1h"));
  }

  @Test
  public void oneSegment() {
    assertEquals(TimeHelper.ONE_DAY * 3, parser.parse("3d"));
  }

  @Test
  public void oneSegmentSingularized() {
    assertEquals(TimeHelper.ONE_DAY, parser.parse("1d"));
  }

  @Test
  public void threeSegments() {
    assertEquals(TimeHelper.ONE_DAY * 3 + TimeHelper.ONE_HOUR + TimeHelper.ONE_MINUTE * 30, parser.parse("3d1h30m"));
  }

  @Test
  public void cumulative() {
    assertEquals(TimeHelper.ONE_MINUTE + TimeHelper.ONE_SECOND * 100, parser.parse("1m100s"));
  }

  @Test
  public void zeroSeconds() {
    assertEquals(0, parser.parse("0d0s"));
  }

  @Test
  public void longPunishment() {
    assertEquals(TimeHelper.ONE_DAY * 446 + TimeHelper.ONE_SECOND, parser.parse("1y2M3w1s"));
  }


  private void assertThrowsException(String message, ThrowingRunnable runnable) {
    CommandValidationException ex = Assert.assertThrows(CommandValidationException.class, runnable);
    Assert.assertEquals(message, ex.getMessage());
  }
}
