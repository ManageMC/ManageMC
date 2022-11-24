package com.managemc.importer.command;

import com.managemc.importer.TestWebClients;
import com.managemc.importer.reader.model.VanillaBan;
import com.managemc.importer.reader.model.VanillaIpBan;
import com.managemc.importer.reader.source.AdvancedBanDb;
import com.managemc.importer.reader.source.MaxBansPlusDb;
import com.managemc.importer.service.OnboardingApiService;
import com.managemc.importer.service.PunishmentImporterService;
import com.managemc.importer.translation.EssentialsTypeTranslator;
import com.managemc.plugins.bukkit.BukkitWrapper;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.bukkit.plugin.Plugin;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openapitools.client.model.ImportableBanOrMute;
import org.openapitools.client.model.ImportableMutes;
import org.openapitools.client.model.ImportablePunishmentPlayer;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CmdImportEssentialsXTest extends CmdImportVanillaTest {

  @Rule
  public TemporaryFolder essentialsFolder = new TemporaryFolder();
  private Path rootFolder;
  private long before;

  private String expectedOffenderUsername;
  private String expectedReason;

  @Before
  public void setup() throws IOException {
    rootFolder = Paths.get(essentialsFolder.getRoot().getPath() + "/userdata");
    Files.createDirectory(rootFolder);
    before = System.currentTimeMillis();
    expectedOffenderUsername = name;
    expectedReason = reason;
  }


  @Test
  public void ifNoPunishments_shouldSkip() {
    assertNoPunishmentsFound();
  }

  @Test
  public void ifNotMuted_shouldSkip() {
    writeMuteToFile(UUID.randomUUID(), false, false, "asd", null, 0);

    assertNoPunishmentsFound();
  }

  @Test
  public void ifNpc_shouldSkip() {
    writeMuteToFile(UUID.randomUUID(), true, true, "asd", null, 0);

    assertNoPunishmentsFound();
  }

  @Test
  public void ifMuteIsOld_shouldSkip() {
    writeMuteToFile(UUID.randomUUID(), true, false, "asd", null, System.currentTimeMillis() - 1);

    assertNoPunishmentsFound();
  }

  @Test
  public void defaultExample() {
    writeMuteToFile(expectedOffenderUuid, name, reason, before + expectedDuration);

    awaitAsyncCommand(this::onCommand);
    assertThatMuteWasImported();
  }

  @Test
  public void permanentMute() {
    writeMuteToFile(expectedOffenderUuid, name, reason, 0);
    expectedDuration = null;

    awaitAsyncCommand(this::onCommand);
    assertThatMuteWasImported();
  }

  @Test
  public void noUsername() {
    writeMuteToFile(expectedOffenderUuid, null, reason, before + expectedDuration);
    expectedOffenderUsername = null;

    awaitAsyncCommand(this::onCommand);
    assertThatMuteWasImported();
  }

  @Test
  public void noReason() {
    writeMuteToFile(expectedOffenderUuid, name, null, before + expectedDuration);
    expectedReason = null;

    awaitAsyncCommand(this::onCommand);
    assertThatMuteWasImported();
  }

  @Test
  public void manyAtOnce_includingMutes() throws Exception {
    VanillaBan[] bans = new VanillaBan[10001];
    IntStream.range(0, 10001).forEach(i -> bans[i] = VanillaBan.builder()
        .uuid(uuid).name(name).created(created)
        .source(source).expires(expires).reason(reason)
        .build());

    File banInput = new File(VANILLA_BANLIST_FILENAME);
    FileUtils.write(banInput, GSON.toJson(bans), "UTF-8");

    VanillaIpBan[] ipBans = new VanillaIpBan[9999];
    IntStream.range(0, 9999).forEach(i -> ipBans[i] = VanillaIpBan.builder()
        .ip(ip).created(created).source(source)
        .expires(expires).reason(reason)
        .build());

    File ipBanInput = new File(VANILLA_IP_BANLIST_FILENAME);
    FileUtils.write(ipBanInput, GSON.toJson(ipBans), "UTF-8");

    IntStream.range(0, 1001).forEach(i -> writeMuteToFile(UUID.randomUUID(), name, reason, 0));

    awaitAsyncCommand(this::onCommand);

    Mockito.verify(sender).sendMessage(ArgumentMatchers.contains("Punishment import job has begun with ID"));
    Mockito.verify(sender).sendMessage(PunishmentImporterService.READING_MESSAGE);
    Mockito.verify(sender).sendMessage(PunishmentImporterService.IMPORTING_MESSAGE);
    Mockito.verify(sender).sendMessage(PunishmentImporterService.FINISHED_MESSAGE);
    Mockito.verify(sender, Mockito.times(4)).sendMessage((String) ArgumentMatchers.any());

    Mockito.verify(onboardingApiService, Mockito.times(51)).importBans(Mockito.eq(IMPORT_ID), Mockito.any());
    Mockito.verify(onboardingApiService, Mockito.times(6)).importMutes(Mockito.anyLong(), Mockito.any());
    Mockito.verify(onboardingApiService, Mockito.never()).importWarnings(Mockito.anyLong(), Mockito.any());
    Mockito.verify(onboardingApiService, Mockito.times(50)).importIpBans(Mockito.eq(IMPORT_ID), Mockito.any());

    Mockito.verify(onboardingApiService).completeImport(IMPORT_ID);

    Mockito.verify(jobTracker, Mockito.times(1)).trackJob(Mockito.any());
  }

  @Test
  public void manyAtOnce_includingMutes_realExample() throws Exception {
    onboardingApiService = new OnboardingApiService(TestWebClients.CLIENT_PROVIDER);

    VanillaBan[] bans = new VanillaBan[201];
    IntStream.range(0, 201).forEach(i -> bans[i] = VanillaBan.builder()
        .uuid(uuid).name(name).created(created)
        .source(source).expires(expires).reason(reason)
        .build());

    File banInput = new File(VANILLA_BANLIST_FILENAME);
    FileUtils.write(banInput, GSON.toJson(bans), "UTF-8");

    VanillaIpBan[] ipBans = new VanillaIpBan[199];
    IntStream.range(0, 199).forEach(i -> ipBans[i] = VanillaIpBan.builder()
        .ip(ip).created(created).source(source)
        .expires(expires).reason(reason)
        .build());

    File ipBanInput = new File(VANILLA_IP_BANLIST_FILENAME);
    FileUtils.write(ipBanInput, GSON.toJson(ipBans), "UTF-8");

    IntStream.range(0, 1001).forEach(i -> writeMuteToFile(UUID.randomUUID(), name, reason, 0));

    awaitAsyncCommand(this::onCommand);

    Mockito.verify(sender).sendMessage(ArgumentMatchers.contains("Punishment import job has begun with ID"));
    Mockito.verify(sender).sendMessage(PunishmentImporterService.READING_MESSAGE);
    Mockito.verify(sender).sendMessage(PunishmentImporterService.IMPORTING_MESSAGE);
    Mockito.verify(sender).sendMessage(PunishmentImporterService.FINISHED_MESSAGE);
    Mockito.verify(sender, Mockito.times(4)).sendMessage((String) ArgumentMatchers.any());

    Mockito.verify(jobTracker, Mockito.times(1)).trackJob(Mockito.any());
  }


  private void writeMuteToFile(UUID uuid, String username, String reason, long muteExpTime) {
    writeMuteToFile(uuid, true, false, username, reason, muteExpTime);
  }

  @SneakyThrows
  private void writeMuteToFile(UUID uuid, boolean muted, boolean npc, String username, String reason, long muteExpTime) {
    Map<String, Object> data = new HashMap<String, Object>() {{
      put("muted", muted);
      put("npc", npc);
      if (username != null) {
        put("last-account-name", username);
      }
      if (reason != null) {
        put("mute-reason", reason);
      }
      put("timestamps", new HashMap<String, Object>() {{
        put("mute", muteExpTime);
      }});
    }};

    String pathForPlayer = rootFolder.toFile().getPath() + "/" + uuid.toString() + ".yml";
    FileWriter writer = new FileWriter(pathForPlayer);
    Yaml yaml = new Yaml();
    yaml.dump(data, writer);
  }

  @Captor
  private ArgumentCaptor<ImportableMutes> mutes;

  @SneakyThrows
  private void assertThatMuteWasImported() {
    Mockito.verify(sender).sendMessage(ArgumentMatchers.contains("Punishment import job has begun with ID"));
    Mockito.verify(sender).sendMessage(PunishmentImporterService.READING_MESSAGE);
    Mockito.verify(sender).sendMessage(PunishmentImporterService.IMPORTING_MESSAGE);
    Mockito.verify(sender).sendMessage(PunishmentImporterService.FINISHED_MESSAGE);
    Mockito.verify(sender, Mockito.times(4)).sendMessage((String) ArgumentMatchers.any());

    ImportablePunishmentPlayer expectedOffender = new ImportablePunishmentPlayer()
        .username(expectedOffenderUsername)
        .uuid(expectedOffenderUuid);

    Mockito.verify(onboardingApiService).importMutes(Mockito.eq(IMPORT_ID), mutes.capture());
    Assert.assertEquals(1, mutes.getValue().getMutes().size());

    ImportableBanOrMute actualMute = mutes.getValue().getMutes().get(0);
    Assert.assertNull(actualMute.getIssuer());
    Assert.assertEquals(expectedOffender, actualMute.getOffender());
    Assert.assertEquals(expectedReason, actualMute.getReason());
    if (expectedDuration == null) {
      Assert.assertNull(actualMute.getDurationMillis());
    } else {
      Assert.assertNotNull(actualMute.getDurationMillis());
      Assert.assertTrue(actualMute.getDurationMillis() <= expectedDuration && actualMute.getDurationMillis() >= expectedDuration - 10 * 60 * 1000);
    }
    Assert.assertEquals(EssentialsTypeTranslator.ESSENTIALS_SOURCE, actualMute.getSource());
    Assert.assertTrue(actualMute.getIssuedAtMillis() >= before && actualMute.getIssuedAtMillis() <= System.currentTimeMillis());

    Mockito.verify(onboardingApiService, Mockito.never()).importBans(Mockito.anyLong(), Mockito.any());
    Mockito.verify(onboardingApiService, Mockito.never()).importWarnings(Mockito.anyLong(), Mockito.any());
    Mockito.verify(onboardingApiService, Mockito.never()).importIpBans(Mockito.anyLong(), Mockito.any());

    Mockito.verify(onboardingApiService).completeImport(IMPORT_ID);

    Mockito.verify(jobTracker).trackJob(Mockito.any());
  }

  @Override
  protected void onCommand() {
    PunishmentImporterService service = new PunishmentImporterService(
        logging,
        Mockito.mock(AdvancedBanDb.class),
        Mockito.mock(MaxBansPlusDb.class),
        onboardingApiService,
        jobTracker
    );

    Plugin essentials = Mockito.mock(Plugin.class);
    Mockito.when(essentials.getDataFolder()).thenReturn(essentialsFolder.getRoot());

    BukkitWrapper wrapper = Mockito.mock(BukkitWrapper.class);
    Mockito.when(wrapper.getOtherPlugin("Essentials")).thenReturn(essentials);

    Mockito.when(config.getImporterService()).thenReturn(service);
    Mockito.when(config.getBukkitWrapper()).thenReturn(wrapper);

    new CmdImport(logging, config).onCommand(sender, command, "", new String[]{"essentials_x"});
  }
}
