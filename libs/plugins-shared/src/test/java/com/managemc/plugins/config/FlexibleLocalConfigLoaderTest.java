package com.managemc.plugins.config;

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

@RunWith(MockitoJUnitRunner.Silent.class)
public class FlexibleLocalConfigLoaderTest {

  private static final String KEY_1 = "ABC";
  private static final String KEY_2 = "DEF";
  private static final String KEY_3 = "GHI";
  private static final String[] KEYS = new String[]{KEY_1, KEY_2, KEY_3};

  private static final File DATA_FOLDER = new File("testdatafolder");
  private static final String FILE_NAME = "foo.yml";
  private static final String CONTENTS = KEY_1 + ":\n" + KEY_2 + ":\n" + KEY_3 + ":\n";

  private SystemPropertiesBasedLocalConfigLoader sysPropsLoader;
  private EnvironmentBasedLocalConfigLoader envLoader;
  private FileBasedLocalConfigLoader fileLoader;
  private FlexibleLocalConfigLoader configLoader;

  @Before
  public void setup() throws IOException {
    FileUtils.deleteDirectory(DATA_FOLDER);

    sysPropsLoader = Mockito.spy(new SystemPropertiesBasedLocalConfigLoader(KEYS));
    envLoader = Mockito.spy(new EnvironmentBasedLocalConfigLoader(KEYS));
    fileLoader = Mockito.spy(new FileBasedLocalConfigLoader(DATA_FOLDER, FILE_NAME, CONTENTS));
    configLoader = new FlexibleLocalConfigLoader(KEYS, sysPropsLoader, envLoader, fileLoader);

    Mockito.when(envLoader.getEnvironmentVariable(Mockito.anyString())).thenReturn(null);
    Mockito.when(sysPropsLoader.getSystemProperty(KEY_1)).thenReturn(null);
    Mockito.when(sysPropsLoader.getSystemProperty(KEY_2)).thenReturn(null);
    Mockito.when(sysPropsLoader.getSystemProperty(KEY_3)).thenReturn(null);
  }

  @After
  public void deleteConfigDirectories() throws IOException {
    FileUtils.deleteDirectory(DATA_FOLDER);
  }


  @Test
  public void nothingConfiguredAnywhere_createsDefaultFileAndReturnsEmptyMap() {
    Assert.assertFalse(DATA_FOLDER.exists());
    Assert.assertFalse(configFile().exists());

    Map<String, Object> result = configLoader.load();

    Assert.assertNull(result.get(KEY_1));
    Assert.assertNull(result.get(KEY_2));
    Assert.assertNull(result.get(KEY_3));

    Assert.assertTrue(DATA_FOLDER.exists());
    Assert.assertTrue(configFile().exists());
    Assert.assertEquals(CONTENTS, readConfigFile());
  }

  @Test
  public void returnsSysPropsIfOnlyThoseAreUsed() {
    Mockito.when(sysPropsLoader.getSystemProperty(KEY_1)).thenReturn("a");
    Mockito.when(sysPropsLoader.getSystemProperty(KEY_2)).thenReturn("b");
    Mockito.when(sysPropsLoader.getSystemProperty(KEY_3)).thenReturn("c");

    Map<String, Object> result = configLoader.load();
    Assert.assertEquals(3, result.size());
    Assert.assertEquals("a", result.get(KEY_1));
    Assert.assertEquals("b", result.get(KEY_2));
    Assert.assertEquals("c", result.get(KEY_3));

    Mockito.verifyNoInteractions(envLoader);
    Mockito.verifyNoInteractions(fileLoader);
  }

  @Test
  public void returnsEnvPropsIfOnlyThoseAreUsed() {
    Mockito.when(envLoader.getEnvironmentVariable(KEY_1)).thenReturn("a");
    Mockito.when(envLoader.getEnvironmentVariable(KEY_2)).thenReturn("b");
    Mockito.when(envLoader.getEnvironmentVariable(KEY_3)).thenReturn("c");

    Map<String, Object> result = configLoader.load();
    Assert.assertEquals(3, result.size());
    Assert.assertEquals("a", result.get(KEY_1));
    Assert.assertEquals("b", result.get(KEY_2));
    Assert.assertEquals("c", result.get(KEY_3));

    Mockito.verify(sysPropsLoader).load();
    Mockito.verifyNoInteractions(fileLoader);
  }

  @Test
  public void returnsFilePropsIfOnlyThoseAreUsed() {
    writeToConfigFile(KEY_1 + ": a\n" + KEY_2 + ": b\n" + KEY_3 + ": c\n");

    Map<String, Object> result = configLoader.load();
    Assert.assertEquals(3, result.size());
    Assert.assertEquals("a", result.get(KEY_1));
    Assert.assertEquals("b", result.get(KEY_2));
    Assert.assertEquals("c", result.get(KEY_3));

    Mockito.verify(sysPropsLoader).load();
    Mockito.verify(envLoader).load();
  }

  @Test
  public void returnsPartsOfEachStrategyAsNeeded() {
    Mockito.when(sysPropsLoader.getSystemProperty(KEY_1)).thenReturn("a");
    Mockito.when(envLoader.getEnvironmentVariable(KEY_1)).thenReturn("oops");
    Mockito.when(envLoader.getEnvironmentVariable(KEY_2)).thenReturn("b");
    writeToConfigFile(KEY_1 + ": oops\n" + KEY_2 + ": oops\n" + KEY_3 + ": c\n");

    Map<String, Object> result = configLoader.load();
    Assert.assertEquals(3, result.size());
    Assert.assertEquals("a", result.get(KEY_1));
    Assert.assertEquals("b", result.get(KEY_2));
    Assert.assertEquals("c", result.get(KEY_3));
  }


  private File configFile() {
    return new File(DATA_FOLDER + "/" + FILE_NAME);
  }

  @SneakyThrows
  private String readConfigFile() {
    return FileUtils.readFileToString(configFile(), "UTF-8");
  }

  @SneakyThrows
  private void writeToConfigFile(String contents) {
    Assert.assertTrue(DATA_FOLDER.mkdir());
    Assert.assertTrue(configFile().createNewFile());

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile()))) {
      writer.write(contents);
    }
  }
}
