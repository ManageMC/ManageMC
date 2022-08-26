package com.managemc.plugins.util;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class UuidHyphenatorTest {

  private static final String JACOB_UUID = "f5eaefb6-60ec-475a-a66a-2c5ceb34a8a0";
  private static final String JACOB_UUID_NO_HYPHENS = "f5eaefb660ec475aa66a2c5ceb34a8a0";

  @Test
  public void uuidWithHyphensAlready() {
    assertEquals(UUID.fromString(JACOB_UUID), UuidHyphenator.toUUID(JACOB_UUID));
  }

  @Test
  public void validUuidWithNoHyphens() {
    assertEquals(UUID.fromString(JACOB_UUID), UuidHyphenator.toUUID(JACOB_UUID_NO_HYPHENS));
  }

  @Test(expected = IllegalArgumentException.class)
  public void invalidUuidWithNoHyphens() {
    UuidHyphenator.toUUID("ayylmao");
  }

  @Test(expected = IllegalArgumentException.class)
  public void emptyStringEdgeCase() {
    UuidHyphenator.toUUID("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullEdgeCase() {
    UuidHyphenator.toUUID(null);
  }
}
