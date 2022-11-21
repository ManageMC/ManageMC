package com.managemc.importer.reader;

import com.managemc.importer.reader.model.EssentialsMute;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EssentialsReader implements PunishmentReader<EssentialsMute> {

  private final File essentialsPluginFolder;

  public EssentialsReader(File essentialsPluginFolder) {
    this.essentialsPluginFolder = essentialsPluginFolder;
  }

  @Override
  public List<EssentialsMute> read() throws Exception {
    File userDataDir = new File(essentialsPluginFolder.getAbsolutePath() + "/userdata");
    if (!userDataDir.exists()) {
      throw new RuntimeException("Folder not found: /plugins/Essentials/userdata/");
    }

    List<EssentialsMute> mutes = new ArrayList<>();
    Path start = Paths.get(new URI("file://" + userDataDir.getAbsolutePath()));
    Yaml yaml = new Yaml();

    Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        try (InputStream inputStream = Files.newInputStream(file)) {
          Map<String, Object> parsedFile = yaml.load(inputStream);
          UUID uuid = UUID.fromString(file.getFileName().toString().split("\\.")[0]);
          EssentialsMute mute = new EssentialsMute(uuid, parsedFile);

          if (qualifiesForImport(mute)) {
            mutes.add(mute);
          }
        }
        return FileVisitResult.CONTINUE;
      }
    });

    return mutes;
  }

  private static boolean qualifiesForImport(EssentialsMute mute) {
    return mute.isMuted() &&
        !mute.isNpc() &&
        (mute.getMute() == 0 || mute.getMute() > System.currentTimeMillis());
  }
}
