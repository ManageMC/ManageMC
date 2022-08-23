package com.managemc.plugins.config;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class FileBasedLocalConfigLoaderTest {

  private static final File DATA_FOLDER = new File("testdatafolder");
  private static final String FILE_NAME = "foo.yml";
  private static final String CONTENTS = "foo: 1\nbar: two\n";

  private LocalConfigLoader localConfigLoader;

  @Before
  public void setup() throws IOException {
    FileUtils.deleteDirectory(DATA_FOLDER);

    localConfigLoader = new FileBasedLocalConfigLoader(DATA_FOLDER, FILE_NAME, CONTENTS);
  }

  @After
  public void deleteConfigDirectories() throws IOException {
    FileUtils.deleteDirectory(DATA_FOLDER);
  }

  @Test
  public void whenDirectoryMissing_createsDirectory() {
    Assert.assertFalse(DATA_FOLDER.exists());
    localConfigLoader.load();
    Assert.assertTrue(DATA_FOLDER.exists());
  }

  @Test
  public void whenConfigFileMissing_createsConfigFile() {
    Assert.assertFalse(configFile().exists());
    localConfigLoader.load();
    Assert.assertTrue(configFile().exists());
  }

  @Test
  public void whenConfigFileMissing_writesDefaultFileContents() throws IOException {
    localConfigLoader.load();
    Assert.assertEquals(CONTENTS, readConfigFile());
  }

  @Test
  public void whenConfigFilePresent_doesNotWriteToConfigFile() throws IOException {
    Assert.assertTrue(DATA_FOLDER.mkdir());
    Assert.assertTrue(configFile().createNewFile());
    localConfigLoader.load();
    Assert.assertEquals("", readConfigFile());
  }

  @Test
  public void parsesConfigFileCorrectly() {
    Map<String, Object> actual = localConfigLoader.load();
    Assert.assertEquals(2, actual.size());
    Assert.assertEquals(1, actual.get("foo"));
    Assert.assertEquals("two", actual.get("bar"));
  }


  private File configFile() {
    return new File(DATA_FOLDER + "/" + FILE_NAME);
  }

  private String readConfigFile() throws IOException {
    return FileUtils.readFileToString(configFile(), "UTF-8");
  }
}
