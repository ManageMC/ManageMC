package com.managemc.importer.util;

import com.managemc.importer.translation.util.TimestampToMillisConverter;
import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;

public class TimestampToMillisConverterTest {

  @Test
  public void example1() throws ParseException {
    String timestamp = "2019-12-26 15:54:21 -0500";
    Assert.assertEquals(1577393661000L, TimestampToMillisConverter.toMillis(timestamp));
  }

  @Test
  public void example2() throws ParseException {
    String timestamp = "2019-12-26 16:21:37 -0500";
    Assert.assertEquals(1577395297000L, TimestampToMillisConverter.toMillis(timestamp));
  }
}
