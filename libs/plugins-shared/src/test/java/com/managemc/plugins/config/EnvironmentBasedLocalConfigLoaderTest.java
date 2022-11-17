package com.managemc.plugins.config;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Map;

@RunWith(MockitoJUnitRunner.Silent.class)
public class EnvironmentBasedLocalConfigLoaderTest {

  private static final String KEY_1 = "ABC";
  private static final String KEY_2 = "DEF";
  private static final String[] KEYS = new String[]{KEY_1, KEY_2};

  private EnvironmentBasedLocalConfigLoader configLoader;

  @Before
  public void setup() {
    configLoader = Mockito.spy(new EnvironmentBasedLocalConfigLoader(KEYS));
    Mockito.when(configLoader.getEnvironmentVariable(KEY_1)).thenReturn(null);
    Mockito.when(configLoader.getEnvironmentVariable(KEY_2)).thenReturn(null);
  }

  @Test
  public void noEnvVarsSet_emptyMap() {
    Assert.assertEquals(0, configLoader.load().size());
  }

  @Test
  public void oneSet_oneInMap() {
    Mockito.when(configLoader.getEnvironmentVariable(KEY_1)).thenReturn("Fred");

    Map<String, Object> result = configLoader.load();

    Assert.assertEquals(1, result.size());
    Assert.assertTrue(result.containsKey(KEY_1));
    Assert.assertEquals("Fred", result.get(KEY_1));
  }

  @Test
  public void bothSet_bothInMap() {
    Mockito.when(configLoader.getEnvironmentVariable(KEY_1)).thenReturn("Fred");
    Mockito.when(configLoader.getEnvironmentVariable(KEY_2)).thenReturn("Mike???");
    // extraneous
    Mockito.when(configLoader.getEnvironmentVariable("oops")).thenReturn("no");

    Map<String, Object> result = configLoader.load();

    Assert.assertEquals(2, result.size());
    Assert.assertTrue(result.containsKey(KEY_1));
    Assert.assertEquals("Fred", result.get(KEY_1));
    Assert.assertTrue(result.containsKey(KEY_2));
    Assert.assertEquals("Mike???", result.get(KEY_2));
  }
}
