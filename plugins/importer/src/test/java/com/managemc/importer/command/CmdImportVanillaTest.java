package com.managemc.importer.command;

import com.google.gson.Gson;
import com.managemc.api.ApiException;
import com.managemc.importer.TestWebClients;
import com.managemc.importer.TestsWithServiceClient;
import com.managemc.importer.config.ManageMCImportPluginConfig;
import com.managemc.importer.data.ImportJobTracker;
import com.managemc.importer.reader.constant.PunishmentReaderConstants;
import com.managemc.importer.reader.model.VanillaBan;
import com.managemc.importer.reader.model.VanillaIpBan;
import com.managemc.importer.reader.source.AdvancedBanDb;
import com.managemc.importer.reader.source.MaxBansPlusDb;
import com.managemc.importer.service.OnboardingApiService;
import com.managemc.importer.service.PunishmentImporterService;
import com.managemc.importer.translation.VanillaTypeTranslator;
import com.managemc.plugins.logging.BukkitLogging;
import org.apache.commons.io.FileUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openapitools.client.model.*;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.IntStream;

@RunWith(MockitoJUnitRunner.class)
public class CmdImportVanillaTest extends TestsWithServiceClient {

  protected static final String VANILLA_BANLIST_FILENAME = "banned-players.json";
  protected static final String VANILLA_IP_BANLIST_FILENAME = "banned-ips.json";

  protected static final Gson GSON = new Gson();

  protected static final long IMPORT_ID = 826;

  protected String ip;
  protected String uuid;
  protected String name;
  protected String created;
  protected String source;
  protected String expires;
  protected String reason;

  protected UUID expectedOffenderUuid;
  protected boolean expectIssuer;
  protected String expectedIssuerUsername;
  protected Long expectedDuration;
  protected Long expectedIssuedAt;

  @Mock
  protected BukkitLogging logging;
  @Mock
  protected ManageMCImportPluginConfig config;
  @Mock
  protected OnboardingApiService onboardingApiService;
  @Mock
  protected CommandSender sender;
  @Mock
  protected Command command;
  @Mock
  protected ImportJobTracker jobTracker;

  @Before
  public void setupVanilla() throws ApiException {
    ip = "1.2.3.4";
    uuid = "f5eaefb6-60ec-475a-a66a-2c5ceb34a8a0";
    name = "JacobCrofts";
    created = "2018-11-23 13:59:45 -0800";
    source = "SomeRandomAdmin";
    expires = "2019-06-01 13:59:45 -0800";
    reason = "cheating and stuff";

    expectedOffenderUuid = UUID.fromString(uuid);
    expectIssuer = true;
    expectedIssuerUsername = source;
    expectedDuration = 16416000000L;
    expectedIssuedAt = 1543010385000L;

    Mockito.when(onboardingApiService.createImport()).thenReturn(IMPORT_ID);
  }

  @After
  public void cleanupVanilla() throws IOException {
    File banList = new File(VANILLA_BANLIST_FILENAME);
    File ipBanList = new File(VANILLA_IP_BANLIST_FILENAME);
    if (banList.exists()) {
      FileUtils.forceDelete(new File(VANILLA_BANLIST_FILENAME));
    }
    if (ipBanList.exists()) {
      FileUtils.forceDelete(ipBanList);
    }
  }

  /**
   * --------------------------
   * Vanilla import test cases:
   * --------------------------
   * - if no ban / ip ban lists exist, the job still runs without error
   * - if the string "forever" is in the "expires" field, duration should be blank
   * - if "expires" is null, duration should be blank
   * - if "Server" or "Console" issued the punishment, issuer attributes are blank
   * - if "source" is not a valid username, issuer attributes are blank
   * - if "reason" is null, reason should be blank
   * - if "name" is null, username should be blank
   * - if UUID is not hyphenated in the db, it will be hyphenated by the plugin
   */

  @Test
  public void noBanLists() {
    // edge case: no ban lists found
    assertNoPunishmentsFound();
  }

  @Test
  public void ban_allNullableAttributesPresent() throws Exception {
    writeBanToFile();
    awaitAsyncCommand(this::onCommand);
    assertThatBanWasImported();
  }

  @Test
  public void ban_allNullableAttributesNull() throws Exception {
    expires = null;
    name = null;
    source = null;
    reason = null;

    expectIssuer = false;
    expectedDuration = null;

    writeBanToFile();
    awaitAsyncCommand(this::onCommand);
    assertThatBanWasImported();
  }

  @Test
  public void ban_issuedByServer() throws Exception {
    source = "Server";

    expectIssuer = false;

    writeBanToFile();
    awaitAsyncCommand(this::onCommand);
    assertThatBanWasImported();
  }

  @Test
  public void ban_issuedByConsole() throws Exception {
    source = "Console";

    expectIssuer = false;

    writeBanToFile();
    awaitAsyncCommand(this::onCommand);
    assertThatBanWasImported();
  }

  @Test
  public void ban_sourceIsInvalidUsername() throws Exception {
    source = "uhhh oops";

    expectIssuer = false;

    writeBanToFile();
    awaitAsyncCommand(this::onCommand);
    assertThatBanWasImported();
  }

  @Test
  public void ban_expiresAfterForever() throws Exception {
    expires = PunishmentReaderConstants.VANILLA_PERMBAN_EXPIRES_FIELD;

    expectedDuration = null;

    writeBanToFile();
    awaitAsyncCommand(this::onCommand);
    assertThatBanWasImported();
  }

  @Test
  public void ban_unhyphenatedUuid() throws Exception {
    uuid = "f5eaefb660ec475aa66a2c5ceb34a8a0";

    writeBanToFile();
    awaitAsyncCommand(this::onCommand);
    assertThatBanWasImported();
  }

  @Test
  public void ipBan_allNullableAttributesPresent() throws Exception {
    writeIpBanToFile();
    awaitAsyncCommand(this::onCommand);
    assertThatIpBanWasImported();
  }

  @Test
  public void ipBan_allNullableAttributesNull() throws Exception {
    expires = null;
    source = null;
    reason = null;

    expectIssuer = false;
    expectedDuration = null;

    writeIpBanToFile();
    awaitAsyncCommand(this::onCommand);
    assertThatIpBanWasImported();
  }

  @Test
  public void ipBan_issuedByServer() throws Exception {
    source = "Server";

    expectIssuer = false;

    writeIpBanToFile();
    awaitAsyncCommand(this::onCommand);
    assertThatIpBanWasImported();
  }

  @Test
  public void ipBan_issuedByConsole() throws Exception {
    source = "Console";

    expectIssuer = false;

    writeIpBanToFile();
    awaitAsyncCommand(this::onCommand);
    assertThatIpBanWasImported();
  }

  @Test
  public void ipBan_sourceIsInvalidUsername() throws Exception {
    source = "uhhh oops";

    expectIssuer = false;

    writeIpBanToFile();
    awaitAsyncCommand(this::onCommand);
    assertThatIpBanWasImported();
  }

  @Test
  public void ipBan_expiresAfterForever() throws Exception {
    expires = PunishmentReaderConstants.VANILLA_PERMBAN_EXPIRES_FIELD;

    expectedDuration = null;

    writeIpBanToFile();
    awaitAsyncCommand(this::onCommand);
    assertThatIpBanWasImported();
  }

  @Test
  public void failedToCreateImport() throws Exception {
    Mockito.doThrow(new ApiException())
        .when(onboardingApiService).createImport();

    writeIpBanToFile();
    awaitAsyncCommand(this::onCommand);

    assertThatImportFailed();

    Mockito.verify(onboardingApiService).createImport();
    Mockito.verifyNoMoreInteractions(onboardingApiService);
  }

  @Test
  public void failedToImportPunishments() throws Exception {
    Mockito.doThrow(new ApiException())
        .when(onboardingApiService).importIpBans(Mockito.eq(IMPORT_ID), Mockito.any());

    writeIpBanToFile();
    awaitAsyncCommand(this::onCommand);

    assertThatImportFailed();

    Mockito.verify(onboardingApiService, Mockito.never()).completeImport(IMPORT_ID);
    Mockito.verify(onboardingApiService).cancelImport(IMPORT_ID);
  }

  @Test
  public void manyAtOnce() throws Exception {
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

    awaitAsyncCommand(this::onCommand);

    Mockito.verify(sender).sendMessage(ArgumentMatchers.contains("Punishment import job has begun with ID"));
    Mockito.verify(sender).sendMessage(PunishmentImporterService.READING_MESSAGE);
    Mockito.verify(sender).sendMessage(PunishmentImporterService.IMPORTING_MESSAGE);
    Mockito.verify(sender).sendMessage(PunishmentImporterService.FINISHED_MESSAGE);
    Mockito.verify(sender, Mockito.times(4)).sendMessage((String) ArgumentMatchers.any());

    Mockito.verify(onboardingApiService, Mockito.times(51)).importBans(Mockito.eq(IMPORT_ID), Mockito.any());
    Mockito.verify(onboardingApiService, Mockito.never()).importMutes(Mockito.anyLong(), Mockito.any());
    Mockito.verify(onboardingApiService, Mockito.never()).importWarnings(Mockito.anyLong(), Mockito.any());
    Mockito.verify(onboardingApiService, Mockito.times(50)).importIpBans(Mockito.eq(IMPORT_ID), Mockito.any());

    Mockito.verify(onboardingApiService).completeImport(IMPORT_ID);

    Mockito.verify(jobTracker, Mockito.times(1)).trackJob(Mockito.any());
  }

  @Test
  public void manyAtOnce_realExample() throws Exception {
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

    awaitAsyncCommand(this::onCommand);

    Mockito.verify(sender).sendMessage(ArgumentMatchers.contains("Punishment import job has begun with ID"));
    Mockito.verify(sender).sendMessage(PunishmentImporterService.READING_MESSAGE);
    Mockito.verify(sender).sendMessage(PunishmentImporterService.IMPORTING_MESSAGE);
    Mockito.verify(sender).sendMessage(PunishmentImporterService.FINISHED_MESSAGE);
    Mockito.verify(sender, Mockito.times(4)).sendMessage((String) ArgumentMatchers.any());

    Mockito.verify(jobTracker, Mockito.times(1)).trackJob(Mockito.any());
  }


  protected void assertNoPunishmentsFound() {
    awaitAsyncCommand(this::onCommand);

    Mockito.verify(sender).sendMessage(ArgumentMatchers.contains("Punishment import job has begun with ID"));
    Mockito.verify(sender).sendMessage(PunishmentImporterService.READING_MESSAGE);
    Mockito.verify(sender).sendMessage(PunishmentImporterService.NO_PUNISHMENTS_FOUND_MESSAGE);
    Mockito.verify(sender, Mockito.times(3)).sendMessage((String) ArgumentMatchers.any());

    Mockito.verifyNoInteractions(onboardingApiService);
  }

  protected void assertThatBanWasImported() throws ApiException {
    Mockito.verify(sender).sendMessage(ArgumentMatchers.contains("Punishment import job has begun with ID"));
    Mockito.verify(sender).sendMessage(PunishmentImporterService.READING_MESSAGE);
    Mockito.verify(sender).sendMessage(PunishmentImporterService.IMPORTING_MESSAGE);
    Mockito.verify(sender).sendMessage(PunishmentImporterService.FINISHED_MESSAGE);
    Mockito.verify(sender, Mockito.times(4)).sendMessage((String) ArgumentMatchers.any());

    ImportablePunishmentPlayer expectedIssuer = expectIssuer
        ? new ImportablePunishmentPlayer().username(expectedIssuerUsername).uuid(null)
        : null;
    ImportablePunishmentPlayer expectedOffender = new ImportablePunishmentPlayer()
        .username(name)
        .uuid(expectedOffenderUuid);
    ImportableBanOrMute ban = new ImportableBanOrMute()
        .issuer(expectedIssuer)
        .offender(expectedOffender)
        .reason(reason)
        .durationMillis(expectedDuration)
        .issuedAtMillis(expectedIssuedAt)
        .source(VanillaTypeTranslator.VANILLA_BAN_LIST_SOURCE);

    ImportableBans expected = new ImportableBans()
        .bans(Collections.singletonList(ban));

    Mockito.verify(onboardingApiService).importBans(IMPORT_ID, expected);
    Mockito.verify(onboardingApiService, Mockito.never()).importMutes(Mockito.anyLong(), Mockito.any());
    Mockito.verify(onboardingApiService, Mockito.never()).importWarnings(Mockito.anyLong(), Mockito.any());
    Mockito.verify(onboardingApiService, Mockito.never()).importIpBans(Mockito.anyLong(), Mockito.any());

    Mockito.verify(onboardingApiService).completeImport(IMPORT_ID);

    Mockito.verify(jobTracker).trackJob(Mockito.any());
  }

  protected void assertThatIpBanWasImported() throws ApiException {
    Mockito.verify(sender).sendMessage(ArgumentMatchers.contains("Punishment import job has begun with ID"));
    Mockito.verify(sender).sendMessage(PunishmentImporterService.READING_MESSAGE);
    Mockito.verify(sender).sendMessage(PunishmentImporterService.IMPORTING_MESSAGE);
    Mockito.verify(sender).sendMessage(PunishmentImporterService.FINISHED_MESSAGE);
    Mockito.verify(sender, Mockito.times(4)).sendMessage((String) ArgumentMatchers.any());

    ImportablePunishmentPlayer expectedIssuer = expectIssuer
        ? new ImportablePunishmentPlayer().username(expectedIssuerUsername).uuid(null)
        : null;
    ImportableIpBan ipBan = new ImportableIpBan()
        .minIpv4Address(ip)
        .maxIpv4Address(ip)
        .issuer(expectedIssuer)
        .reason(reason)
        .durationMillis(expectedDuration)
        .issuedAtMillis(expectedIssuedAt)
        .source(VanillaTypeTranslator.VANILLA_IP_BAN_LIST_SOURCE);

    ImportableIpBans expected = new ImportableIpBans()
        .ipBans(Collections.singletonList(ipBan));

    Mockito.verify(onboardingApiService, Mockito.never()).importBans(Mockito.anyLong(), Mockito.any());
    Mockito.verify(onboardingApiService, Mockito.never()).importMutes(Mockito.anyLong(), Mockito.any());
    Mockito.verify(onboardingApiService, Mockito.never()).importWarnings(Mockito.anyLong(), Mockito.any());
    Mockito.verify(onboardingApiService).importIpBans(IMPORT_ID, expected);

    Mockito.verify(onboardingApiService).completeImport(IMPORT_ID);

    Mockito.verify(jobTracker).trackJob(Mockito.any());
  }

  protected void assertThatImportFailed() {
    Mockito.verify(sender).sendMessage(ArgumentMatchers.contains("Punishment import job has begun with ID"));
    Mockito.verify(sender).sendMessage(PunishmentImporterService.READING_MESSAGE);
    Mockito.verify(sender).sendMessage(PunishmentImporterService.IMPORTING_MESSAGE);
    Mockito.verify(sender).sendMessage(PunishmentImporterService.IMPORT_FAILED_MESSAGE);
    Mockito.verify(sender, Mockito.times(4)).sendMessage((String) ArgumentMatchers.any());

    Mockito.verify(logging).logStackTrace(Mockito.any());

    Mockito.verify(jobTracker).trackJob(Mockito.any());
  }

  protected void writeBanToFile() throws IOException {
    VanillaBan[] bans = new VanillaBan[]{
        VanillaBan.builder()
            .uuid(uuid)
            .name(name)
            .created(created)
            .source(source)
            .expires(expires)
            .reason(reason)
            .build(),
    };

    File banInput = new File(VANILLA_BANLIST_FILENAME);
    FileUtils.write(banInput, GSON.toJson(bans), "UTF-8");
  }

  protected void writeIpBanToFile() throws IOException {
    VanillaIpBan[] ipBans = new VanillaIpBan[]{
        VanillaIpBan.builder()
            .ip(ip)
            .created(created)
            .source(source)
            .expires(expires)
            .reason(reason)
            .build(),
    };

    File ipBanInput = new File(VANILLA_IP_BANLIST_FILENAME);
    FileUtils.write(ipBanInput, GSON.toJson(ipBans), "UTF-8");
  }

  protected void onCommand() {
    PunishmentImporterService service = new PunishmentImporterService(
        logging,
        Mockito.mock(AdvancedBanDb.class),
        Mockito.mock(MaxBansPlusDb.class),
        onboardingApiService,
        jobTracker
    );

    Mockito.when(config.getImporterService()).thenReturn(service);

    new CmdImport(logging, config).onCommand(sender, command, "", new String[]{"vanilla"});
  }
}
