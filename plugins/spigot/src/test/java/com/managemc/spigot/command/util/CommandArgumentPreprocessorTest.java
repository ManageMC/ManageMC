package com.managemc.spigot.command.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CommandArgumentPreprocessorTest {

  @Test
  public void noArgs() {
    Assert.assertArrayEquals(arrayOf(), processDoubleQuotes());
  }

  @Test
  public void oneArg() {
    Assert.assertArrayEquals(arrayOf("asd"), processDoubleQuotes("asd"));
  }

  @Test
  public void oneArg_withStartQuote() {
    Assert.assertArrayEquals(arrayOf("\"asd"), processDoubleQuotes("\"asd"));
  }

  @Test
  public void oneArg_withMiddleQuote() {
    Assert.assertArrayEquals(arrayOf("a\"sd"), processDoubleQuotes("a\"sd"));
  }

  @Test
  public void oneArg_withEndQuote() {
    Assert.assertArrayEquals(arrayOf("asd\""), processDoubleQuotes("asd\""));
  }

  @Test
  public void oneArg_isolatedQuote() {
    Assert.assertArrayEquals(arrayOf("\""), processDoubleQuotes("\""));
  }

  @Test
  public void oneArg_emptyQuotes() {
    Assert.assertArrayEquals(arrayOf("\"\""), processDoubleQuotes("\"\""));
  }

  @Test
  public void oneArg_emptyQuotesAroundWord() {
    Assert.assertArrayEquals(arrayOf("\"asd\""), processDoubleQuotes("\"asd\""));
  }

  @Test
  public void twoArgs() {
    Assert.assertArrayEquals(arrayOf("asd", "sdf"), processDoubleQuotes("asd", "sdf"));
  }

  @Test
  public void twoArgs_startQuoteOnly() {
    Assert.assertArrayEquals(arrayOf("\"asd", "sdf"), processDoubleQuotes("\"asd", "sdf"));
  }

  @Test
  public void twoArgs_endQuoteOnly() {
    Assert.assertArrayEquals(arrayOf("asd", "sdf\""), processDoubleQuotes("asd", "sdf\""));
  }

  @Test
  public void twoArgs_middleQuoteOnly() {
    Assert.assertArrayEquals(arrayOf("asd\"", "sdf"), processDoubleQuotes("asd\"", "sdf"));
    Assert.assertArrayEquals(arrayOf("asd", "\"sdf"), processDoubleQuotes("asd", "\"sdf"));
    Assert.assertArrayEquals(arrayOf("a\"sd", "sdf"), processDoubleQuotes("a\"sd", "sdf"));
    Assert.assertArrayEquals(arrayOf("asd", "sd\"f"), processDoubleQuotes("asd", "sd\"f"));
  }

  @Test
  public void twoArgs_isolatedStartQuote() {
    Assert.assertArrayEquals(arrayOf("\"", "sdf"), processDoubleQuotes("\"", "sdf"));
  }

  @Test
  public void twoArgs_isolatedEndQuote() {
    Assert.assertArrayEquals(arrayOf("asd", "\""), processDoubleQuotes("asd", "\""));
  }

  @Test
  public void twoArgs_bothIsolatedQuotes() {
    Assert.assertArrayEquals(arrayOf(), processDoubleQuotes("\"", "\""));
  }

  @Test
  public void twoArgs_surroundedByQuotes() {
    Assert.assertArrayEquals(arrayOf("asd sdf"), processDoubleQuotes("\"asd", "sdf\""));
  }

  @Test
  public void twoArgs_isolatedStartQuote_withEndQuote() {
    Assert.assertArrayEquals(arrayOf("sdf"), processDoubleQuotes("\"", "sdf\""));
  }

  @Test
  public void twoArgs_isolatedEndQuote_withStartQuote() {
    Assert.assertArrayEquals(arrayOf("asd"), processDoubleQuotes("\"asd", "\""));
  }

  @Test
  public void threeArgs_middleQuotesOnly() {
    Assert.assertArrayEquals(arrayOf("a\"sd", "123", "sd\"f"), processDoubleQuotes("a\"sd", "123", "sd\"f"));
  }

  @Test
  public void threeArgs_startAndMiddleQuotes() {
    Assert.assertArrayEquals(arrayOf("\"asd", "123", "sd\"f"), processDoubleQuotes("\"asd", "123", "sd\"f"));
  }

  @Test
  public void threeArgs_middleAndEndQuotes() {
    Assert.assertArrayEquals(arrayOf("as\"d", "123", "sdf\""), processDoubleQuotes("as\"d", "123", "sdf\""));
  }

  @Test
  public void threeArgs_examplesWithValidQuotes() {
    Assert.assertArrayEquals(arrayOf("asd 123 sdf"), processDoubleQuotes("\"asd", "123", "sdf\""));
    Assert.assertArrayEquals(arrayOf("asd 123", "sdf"), processDoubleQuotes("\"asd", "123\"", "sdf"));
    Assert.assertArrayEquals(arrayOf("asd", "123 sdf"), processDoubleQuotes("asd", "\"123", "sdf\""));
  }

  @Test
  public void complicatedExample() {
    Assert.assertArrayEquals(
        arrayOf("123 4\"56 789 qwe", "rty uio", "p\""),
        processDoubleQuotes("\"123", "4\"56", "789", "qwe", "\"", "\"", "rty", "uio\"", "p\"")
    );
  }

  @Test
  public void finishedTyping_noArgs() {
    Assert.assertTrue(processCommandArgs(new HashSet<>(), arrayOf()).isFinishedTyping());
  }

  @Test
  public void finishedTyping_oneArg_notSpace() {
    Assert.assertFalse(processCommandArgs(new HashSet<>(), arrayOf("asd")).isFinishedTyping());
  }

  @Test
  public void finishedTyping_oneArg_space() {
    Assert.assertTrue(processCommandArgs(new HashSet<>(), arrayOf("")).isFinishedTyping());
  }

  @Test
  public void finishedTyping_twoArgs_notSpace() {
    Assert.assertFalse(processCommandArgs(new HashSet<>(), arrayOf("asd", "fgh")).isFinishedTyping());
  }

  @Test
  public void finishedTyping_twoArgs_space() {
    Assert.assertTrue(processCommandArgs(new HashSet<>(), arrayOf("asd", "")).isFinishedTyping());
  }

  @Test
  public void strippingFlags_noFlags() {
    ProcessedCommandArguments args = processCommandArgs(new HashSet<>(), arrayOf("--fake", "--news=1", "-x", "-y=4"));
    Assert.assertArrayEquals(arrayOf("--fake", "--news=1", "-x", "-y=4"), args.getArgs());
    Assert.assertEquals(0, args.getFlags().size());
  }

  @Test
  public void strippingFlags_withFlagsAndOtherThings() {
    Set<CommandFlag> flags = new HashSet<>(
        Arrays.asList(
            CommandFlag.defaultFlag("fake"),
            CommandFlag.argumentFlag("news"),
            CommandFlag.defaultFlag("xxx"),
            CommandFlag.argumentFlag("yyy"),
            CommandFlag.defaultFlag("zzz")
        )
    );
    ProcessedCommandArguments args = processCommandArgs(flags, arrayOf("hello", "--fake", "--news=1", "-x", "Fred", "-y=4"));
    Assert.assertArrayEquals(arrayOf("hello", "Fred"), args.getArgs());
    Assert.assertEquals(4, args.getFlags().size());
    Assert.assertEquals("", args.getFlag("fake"));
    Assert.assertEquals("1", args.getFlag("news"));
    Assert.assertEquals("", args.getFlag("xxx"));
    Assert.assertEquals("4", args.getFlag("yyy"));
    Assert.assertNull(args.getFlag("zzz"));
  }

  @Test
  public void strippingFlags_flagWithinQuotes() {
    Set<CommandFlag> flags = new HashSet<>(
        Arrays.asList(
            CommandFlag.defaultFlag("fake"),
            CommandFlag.defaultFlag("news")
        )
    );
    ProcessedCommandArguments args = processCommandArgs(flags, arrayOf("\"hello", "--fake", "\"", "-n", "Fred"));
    Assert.assertArrayEquals(arrayOf("hello --fake", "Fred"), args.getArgs());
    Assert.assertEquals(1, args.getFlags().size());
    Assert.assertNull(args.getFlag("fake"));
    Assert.assertEquals("", args.getFlag("news"));
  }


  private String[] processDoubleQuotes(String... args) {
    return processCommandArgs(new HashSet<>(), args).getArgs();
  }

  private ProcessedCommandArguments processCommandArgs(Collection<CommandFlag> flags, String... args) {
    return CommandArgumentPreprocessor.preProcessArguments(flags, args);
  }

  private String[] arrayOf(String... args) {
    return args;
  }
}
