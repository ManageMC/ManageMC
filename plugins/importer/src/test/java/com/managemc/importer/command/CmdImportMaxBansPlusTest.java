package com.managemc.importer.command;

import com.managemc.api.ApiException;
import com.managemc.importer.TestsWithServiceClient;
import com.managemc.importer.config.ManageMCImportPluginConfig;
import com.managemc.importer.data.ImportJobTracker;
import com.managemc.importer.reader.source.AdvancedBanDb;
import com.managemc.importer.reader.source.MaxBansPlusDb;
import com.managemc.importer.service.OnboardingApiService;
import com.managemc.importer.service.PunishmentImporterService;
import com.managemc.importer.translation.MaxBansTypeTranslator;
import com.managemc.plugins.logging.BukkitLogging;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.maxgamer.maxbans.orm.Warning;
import org.maxgamer.maxbans.orm.*;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openapitools.client.model.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class CmdImportMaxBansPlusTest extends TestsWithServiceClient {

  private static final long IMPORT_ID = 801;

  @Mock
  private BukkitLogging logging;
  @Mock
  private ManageMCImportPluginConfig config;
  @Mock
  private CommandSender sender;
  @Mock
  private MaxBansPlusDb db;
  @Mock
  private OnboardingApiService onboardingApiService;
  @Mock
  private ImportJobTracker jobTracker;

  private String maxBansMainClass;

  private long created;
  private Long expiresAt;
  private Long revokedAt;
  private String reason;
  private String issuerUsername;
  private String revokerUsername;
  private String offenderUsernameOrIp;

  private String expectedIssuerUsername;
  private String expectedRevokerUsername;

  private List<Ban> bans;
  private List<Mute> mutes;
  private List<Warning> warnings;

  @Before
  public void setup() throws ApiException {
    maxBansMainClass = "org.maxgamer.maxbans.MaxBansPlus";

    created = 1600000000000L;
    expiresAt = null;
    revokedAt = null;
    reason = "cheating";
    issuerUsername = null;
    revokerUsername = null;
    offenderUsernameOrIp = "JacobCrofts";

    expectedIssuerUsername = null;
    expectedRevokerUsername = null;

    bans = new ArrayList<>();
    mutes = new ArrayList<>();
    warnings = new ArrayList<>();

    Mockito.when(db.getAllBans()).thenReturn(bans);
    Mockito.when(db.getAllMutes()).thenReturn(mutes);
    Mockito.when(db.getAllWarnings()).thenReturn(warnings);

    Mockito.when(onboardingApiService.createImport()).thenReturn(IMPORT_ID);
  }

  @Test
  public void mainClassNotFound() {
    maxBansMainClass = "com.oops.notreal";
    onCommand("max_bans_plus");
    Mockito.verify(sender).sendMessage(ArgumentMatchers.contains(ChatColor.RED +
        "Unable to find the MaxBansPlus plugin running locally"));
    Mockito.verify(sender, Mockito.times(1)).sendMessage((String) ArgumentMatchers.any());
  }

  @Test
  public void noPunishments() {
    Mockito.when(db.getAllBans()).thenReturn(Collections.emptyList());
    Mockito.when(db.getAllMutes()).thenReturn(Collections.emptyList());
    Mockito.when(db.getAllWarnings()).thenReturn(Collections.emptyList());

    awaitAsyncCommand(() -> onCommand("MAX_BANS_PLUS"));

    Mockito.verify(sender).sendMessage(ArgumentMatchers.contains("Punishment import job has begun with ID"));
    Mockito.verify(sender).sendMessage(PunishmentImporterService.READING_MESSAGE);
    Mockito.verify(sender).sendMessage(PunishmentImporterService.NO_PUNISHMENTS_FOUND_MESSAGE);
    Mockito.verify(sender, Mockito.times(3)).sendMessage((String) ArgumentMatchers.any());

    Mockito.verifyNoInteractions(onboardingApiService);
  }

  /**
   * ------------------------------
   * MaxBansPlus import test cases:
   * ------------------------------
   * - if "source" field is null, issuer should be null
   * - if "reason" field is null, the corresponding parameter should be null
   * - if "source" field is not a valid username, issuer should be null
   * - if "revoker" field is not a valid username, issuer should be null
   * - should ignore IP mutes for now
   * - all warnings are assumed to be ban warnings
   */

  @Test
  public void ban_allAttributesPresent() throws ApiException {
    expiresAt = created + 500_000;
    revokedAt = created + 700_000;
    reason = "cheating";
    issuerUsername = "AuntPhyllis";
    revokerUsername = "hclewk";
    expectedIssuerUsername = "AuntPhyllis";
    expectedRevokerUsername = "hclewk";

    bans.add(buildBan());

    awaitAsyncCommand(() -> onCommand("MAX_BANS_PLUS"));

    assertThatBanWasImported();
  }

  @Test
  public void ban_nullableAttributesAllMissing() throws ApiException {
    issuerUsername = null;
    expectedIssuerUsername = null;
    reason = null;
    bans.add(buildBan());

    awaitAsyncCommand(() -> onCommand("MAX_BANS_PLUS"));

    assertThatBanWasImported();
  }

  @Test
  public void ban_sourceHasInvalidUsername() throws ApiException {
    issuerUsername = "uh... oops";
    expectedIssuerUsername = null;
    bans.add(buildBan());

    awaitAsyncCommand(() -> onCommand("MAX_BANS_PLUS"));

    assertThatBanWasImported();
  }

  @Test
  public void ban_revokerHasInvalidUsername() throws ApiException {
    revokedAt = created + 700_000;
    revokerUsername = "uh... oops";
    expectedRevokerUsername = null;
    bans.add(buildBan());

    awaitAsyncCommand(() -> onCommand("MAX_BANS_PLUS"));

    assertThatBanWasImported();
  }

  @Test
  public void mute_allAttributesPresent() throws ApiException {
    expiresAt = created + 500_000;
    revokedAt = created + 700_000;
    reason = "cheating";
    issuerUsername = "AuntPhyllis";
    revokerUsername = "hclewk";
    expectedIssuerUsername = "AuntPhyllis";
    expectedRevokerUsername = "hclewk";

    mutes.add(buildMute());

    awaitAsyncCommand(() -> onCommand("MAX_BANS_PLUS"));

    assertThatMuteWasImported();
  }

  @Test
  public void mute_nullableAttributesAllMissing() throws ApiException {
    issuerUsername = null;
    expectedIssuerUsername = null;
    reason = null;
    mutes.add(buildMute());

    awaitAsyncCommand(() -> onCommand("MAX_BANS_PLUS"));

    assertThatMuteWasImported();
  }

  @Test
  public void mute_sourceHasInvalidUsername() throws ApiException {
    issuerUsername = "uh... oops";
    expectedIssuerUsername = null;
    mutes.add(buildMute());

    awaitAsyncCommand(() -> onCommand("MAX_BANS_PLUS"));

    assertThatMuteWasImported();
  }

  @Test
  public void mute_revokerHasInvalidUsername() throws ApiException {
    revokedAt = created + 700_000;
    revokerUsername = "uh... oops";
    expectedRevokerUsername = null;
    mutes.add(buildMute());

    awaitAsyncCommand(() -> onCommand("MAX_BANS_PLUS"));

    assertThatMuteWasImported();
  }

  @Test
  public void ipBan_allAttributesPresent() throws ApiException {
    offenderUsernameOrIp = "1.2.3.4";
    expiresAt = created + 500_000;
    revokedAt = created + 700_000;
    reason = "cheating";
    issuerUsername = "AuntPhyllis";
    revokerUsername = "hclewk";
    expectedIssuerUsername = "AuntPhyllis";
    expectedRevokerUsername = "hclewk";

    bans.add(buildBan());

    awaitAsyncCommand(() -> onCommand("MAX_BANS_PLUS"));

    assertThatIpBanWasImported();
  }

  @Test
  public void ipBan_nullableAttributesAllMissing() throws ApiException {
    offenderUsernameOrIp = "1.2.3.4";
    issuerUsername = null;
    expectedIssuerUsername = null;
    reason = null;
    bans.add(buildBan());

    awaitAsyncCommand(() -> onCommand("MAX_BANS_PLUS"));

    assertThatIpBanWasImported();
  }

  @Test
  public void ipBan_sourceHasInvalidUsername() throws ApiException {
    offenderUsernameOrIp = "1.2.3.4";
    issuerUsername = "uh... oops";
    expectedIssuerUsername = null;
    bans.add(buildBan());

    awaitAsyncCommand(() -> onCommand("MAX_BANS_PLUS"));

    assertThatIpBanWasImported();
  }

  @Test
  public void ipBan_revokerHasInvalidUsername() throws ApiException {
    offenderUsernameOrIp = "1.2.3.4";
    revokedAt = created + 700_000;
    revokerUsername = "uh... oops";
    expectedRevokerUsername = null;
    bans.add(buildBan());

    awaitAsyncCommand(() -> onCommand("MAX_BANS_PLUS"));

    assertThatIpBanWasImported();
  }

  @Test
  public void warning_allAttributesPresent() throws ApiException {
    revokedAt = created + 700_000;
    reason = "cheating";
    issuerUsername = "AuntPhyllis";
    revokerUsername = "hclewk";
    expectedIssuerUsername = "AuntPhyllis";
    expectedRevokerUsername = "hclewk";

    warnings.add(buildWarning());

    awaitAsyncCommand(() -> onCommand("MAX_BANS_PLUS"));

    assertThatWarningWasImported();
  }

  @Test
  public void warning_nullableAttributesAllMissing() throws ApiException {
    issuerUsername = null;
    expectedIssuerUsername = null;
    reason = null;
    warnings.add(buildWarning());

    awaitAsyncCommand(() -> onCommand("MAX_BANS_PLUS"));

    assertThatWarningWasImported();
  }

  @Test
  public void warning_sourceHasInvalidUsername() throws ApiException {
    issuerUsername = "uh... oops";
    expectedIssuerUsername = null;
    warnings.add(buildWarning());

    awaitAsyncCommand(() -> onCommand("MAX_BANS_PLUS"));

    assertThatWarningWasImported();
  }

  @Test
  public void warning_revokerHasInvalidUsername() throws ApiException {
    revokedAt = created + 700_000;
    revokerUsername = "uh... oops";
    expectedRevokerUsername = null;
    warnings.add(buildWarning());

    awaitAsyncCommand(() -> onCommand("MAX_BANS_PLUS"));

    assertThatWarningWasImported();
  }

  @Test
  public void shouldIgnoreIpMutes() {
    offenderUsernameOrIp = "1.1.1.1";
    mutes.add(buildMute());

    awaitAsyncCommand(() -> onCommand("MAX_BANS_PLUS"));

    Mockito.verify(sender).sendMessage(ArgumentMatchers.contains("Punishment import job has begun with ID"));
    Mockito.verify(sender).sendMessage(PunishmentImporterService.READING_MESSAGE);
    Mockito.verify(sender).sendMessage(PunishmentImporterService.NO_PUNISHMENTS_FOUND_MESSAGE);
    Mockito.verify(sender, Mockito.times(3)).sendMessage((String) ArgumentMatchers.any());

    Mockito.verifyNoInteractions(onboardingApiService);
  }

  @Test
  public void failedToCreateImport() throws Exception {
    Mockito.doThrow(new ApiException())
        .when(onboardingApiService).createImport();

    bans.add(buildBan());
    awaitAsyncCommand(() -> onCommand("MAX_BANS_PLUS"));

    assertThatImportFailed();

    Mockito.verify(onboardingApiService).createImport();
    Mockito.verifyNoMoreInteractions(onboardingApiService);
  }

  @Test
  public void failedToImportPunishments() throws Exception {
    Mockito.doThrow(new ApiException())
        .when(onboardingApiService).importBans(Mockito.eq(IMPORT_ID), Mockito.any());

    bans.add(buildBan());
    awaitAsyncCommand(() -> onCommand("MAX_BANS_PLUS"));

    assertThatImportFailed();

    Mockito.verify(onboardingApiService, Mockito.never()).completeImport(IMPORT_ID);
    Mockito.verify(onboardingApiService).cancelImport(IMPORT_ID);
  }


  private void assertThatBanWasImported() throws ApiException {
    assertThatSuccessMessagesWereSent();

    ImportableBans expected = new ImportableBans()
        .bans(Collections.singletonList(expectedBanOrMute()));

    Mockito.verify(onboardingApiService).importBans(IMPORT_ID, expected);
    Mockito.verify(onboardingApiService, Mockito.never()).importMutes(Mockito.anyLong(), Mockito.any());
    Mockito.verify(onboardingApiService, Mockito.never()).importWarnings(Mockito.anyLong(), Mockito.any());
    Mockito.verify(onboardingApiService, Mockito.never()).importIpBans(Mockito.anyLong(), Mockito.any());

    Mockito.verify(jobTracker).trackJob(Mockito.any());

    Mockito.verify(onboardingApiService).completeImport(IMPORT_ID);
  }

  private void assertThatMuteWasImported() throws ApiException {
    assertThatSuccessMessagesWereSent();

    ImportableMutes expected = new ImportableMutes()
        .mutes(Collections.singletonList(expectedBanOrMute()));

    Mockito.verify(onboardingApiService, Mockito.never()).importBans(Mockito.anyLong(), Mockito.any());
    Mockito.verify(onboardingApiService).importMutes(IMPORT_ID, expected);
    Mockito.verify(onboardingApiService, Mockito.never()).importWarnings(Mockito.anyLong(), Mockito.any());
    Mockito.verify(onboardingApiService, Mockito.never()).importIpBans(Mockito.anyLong(), Mockito.any());

    Mockito.verify(jobTracker).trackJob(Mockito.any());

    Mockito.verify(onboardingApiService).completeImport(IMPORT_ID);
  }

  private void assertThatWarningWasImported() throws ApiException {
    assertThatSuccessMessagesWereSent();

    ImportableWarnings expected = new ImportableWarnings()
        .warnings(Collections.singletonList(expectedWarning()));

    Mockito.verify(onboardingApiService, Mockito.never()).importBans(Mockito.anyLong(), Mockito.any());
    Mockito.verify(onboardingApiService, Mockito.never()).importMutes(Mockito.anyLong(), Mockito.any());
    Mockito.verify(onboardingApiService).importWarnings(IMPORT_ID, expected);
    Mockito.verify(onboardingApiService, Mockito.never()).importIpBans(Mockito.anyLong(), Mockito.any());

    Mockito.verify(jobTracker).trackJob(Mockito.any());

    Mockito.verify(onboardingApiService).completeImport(IMPORT_ID);
  }

  private void assertThatIpBanWasImported() throws ApiException {
    assertThatSuccessMessagesWereSent();

    ImportableIpBans expected = new ImportableIpBans()
        .ipBans(Collections.singletonList(expectedIpBan()));

    Mockito.verify(onboardingApiService, Mockito.never()).importBans(Mockito.anyLong(), Mockito.any());
    Mockito.verify(onboardingApiService, Mockito.never()).importMutes(Mockito.anyLong(), Mockito.any());
    Mockito.verify(onboardingApiService, Mockito.never()).importWarnings(Mockito.anyLong(), Mockito.any());
    Mockito.verify(onboardingApiService).importIpBans(IMPORT_ID, expected);

    Mockito.verify(jobTracker).trackJob(Mockito.any());

    Mockito.verify(onboardingApiService).completeImport(IMPORT_ID);
  }

  private void assertThatImportFailed() {
    Mockito.verify(sender).sendMessage(ArgumentMatchers.contains("Punishment import job has begun with ID"));
    Mockito.verify(sender).sendMessage(PunishmentImporterService.READING_MESSAGE);
    Mockito.verify(sender).sendMessage(PunishmentImporterService.IMPORTING_MESSAGE);
    Mockito.verify(sender).sendMessage(PunishmentImporterService.IMPORT_FAILED_MESSAGE);
    Mockito.verify(sender, Mockito.times(4)).sendMessage((String) ArgumentMatchers.any());

    Mockito.verify(logging).logStackTrace(Mockito.any());

    Mockito.verify(jobTracker).trackJob(Mockito.any());
  }

  private ImportableBanOrMute expectedBanOrMute() {
    return new ImportableBanOrMute()
        .offender(expectedOffender())
        .issuer(expectedIssuer())
        .visibility(PunishmentVisibility.PUBLIC)
        .scope(null)
        .reason(reason)
        .details(null)
        .durationMillis(expiresAt == null ? null : expiresAt - created)
        .issuedAtMillis(created)
        .pardon(expectedPardon())
        .source(MaxBansTypeTranslator.SOURCE);
  }

  private ImportableWarning expectedWarning() {
    return new ImportableWarning()
        .offender(expectedOffender())
        .issuer(expectedIssuer())
        .reason(reason)
        .details(null)
        .issuedAtMillis(created)
        .pardon(expectedPardon())
        .source(MaxBansTypeTranslator.SOURCE);
  }

  private ImportableIpBan expectedIpBan() {
    return new ImportableIpBan()
        .minIpv4Address(offenderUsernameOrIp)
        .maxIpv4Address(offenderUsernameOrIp)
        .issuer(expectedIssuer())
        .visibility(PunishmentVisibility.PUBLIC)
        .scope(null)
        .reason(reason)
        .details(null)
        .durationMillis(expiresAt == null ? null : expiresAt - created)
        .issuedAtMillis(created)
        .pardon(expectedPardon())
        .source(MaxBansTypeTranslator.SOURCE);
  }

  private void assertThatSuccessMessagesWereSent() {
    Mockito.verify(sender).sendMessage(ArgumentMatchers.contains("Punishment import job has begun with ID"));
    Mockito.verify(sender).sendMessage(PunishmentImporterService.READING_MESSAGE);
    Mockito.verify(sender).sendMessage(PunishmentImporterService.IMPORTING_MESSAGE);
    Mockito.verify(sender).sendMessage(PunishmentImporterService.FINISHED_MESSAGE);
    Mockito.verify(sender, Mockito.times(4)).sendMessage((String) ArgumentMatchers.any());
  }

  private ImportablePunishmentPlayer expectedIssuer() {
    return expectedIssuerUsername == null
        ? null
        : new ImportablePunishmentPlayer().username(expectedIssuerUsername).uuid(null);
  }

  private ImportablePunishmentPlayer expectedOffender() {
    return new ImportablePunishmentPlayer()
        .username(offenderUsernameOrIp)
        .uuid(null);
  }

  private ImportablePunishmentPardon expectedPardon() {
    if (revokedAt != null) {
      ImportablePunishmentPlayer pardoner = expectedRevokerUsername == null
          ? null
          : new ImportablePunishmentPlayer().username(expectedRevokerUsername).uuid(null);
      return new ImportablePunishmentPardon()
          .pardoner(pardoner)
          .pardonedAtMillis(revokedAt)
          .details(MaxBansTypeTranslator.PARDON_DETAILS);
    }
    return null;
  }

  private MockBan buildBan() {
    return MockBan.builder()
        .created(Instant.ofEpochMilli(created))
        .expiresAt(expiresAt == null ? null : Instant.ofEpochMilli(expiresAt))
        .revokedAt(revokedAt == null ? null : Instant.ofEpochMilli(revokedAt))
        .reason(reason)
        .source(issuerUsername == null ? null : new User(UUID.randomUUID(), issuerUsername))
        .revoker(revokerUsername == null ? null : new User(UUID.randomUUID(), revokerUsername))
        .tenant(new User(UUID.randomUUID(), offenderUsernameOrIp))
        .build();
  }

  private MockMute buildMute() {
    return MockMute.builder()
        .created(Instant.ofEpochMilli(created))
        .expiresAt(expiresAt == null ? null : Instant.ofEpochMilli(expiresAt))
        .revokedAt(revokedAt == null ? null : Instant.ofEpochMilli(revokedAt))
        .reason(reason)
        .source(issuerUsername == null ? null : new User(UUID.randomUUID(), issuerUsername))
        .revoker(revokerUsername == null ? null : new User(UUID.randomUUID(), revokerUsername))
        .tenant(new User(UUID.randomUUID(), offenderUsernameOrIp))
        .build();
  }

  private MockWarning buildWarning() {
    return MockWarning.builder()
        .created(Instant.ofEpochMilli(created))
        .expiresAt(expiresAt == null ? null : Instant.ofEpochMilli(expiresAt))
        .revokedAt(revokedAt == null ? null : Instant.ofEpochMilli(revokedAt))
        .reason(reason)
        .source(issuerUsername == null ? null : new User(UUID.randomUUID(), issuerUsername))
        .revoker(revokerUsername == null ? null : new User(UUID.randomUUID(), revokerUsername))
        .tenant(new User(UUID.randomUUID(), offenderUsernameOrIp))
        .build();
  }

  private void onCommand(String... args) {
    PunishmentImporterService service = new PunishmentImporterService(
        logging,
        Mockito.mock(AdvancedBanDb.class),
        db,
        onboardingApiService,
        jobTracker
    );

    Mockito.when(config.getImporterService()).thenReturn(service);
    Mockito.when(config.getMaxBansMainClass()).thenReturn(maxBansMainClass);

    new CmdImport(logging, config).execute(sender, "nobodycares", args);
  }

  @Builder
  private static class MockBan extends Ban {

    // these getters quietly override superclass methods
    @Getter
    private final Instant created;
    @Getter
    private final Instant expiresAt;
    @Getter
    private final Instant revokedAt;
    @Getter
    private final String reason;
    @Getter
    private final User source;
    @Getter
    private final User revoker;
    @Getter
    private final Tenant tenant;
  }

  @Builder
  private static class MockMute extends Mute {

    @Getter
    private final Instant created;
    @Getter
    private final Instant expiresAt;
    @Getter
    private final Instant revokedAt;
    @Getter
    private final String reason;
    @Getter
    private final User source;
    @Getter
    private final User revoker;
    @Getter
    private final Tenant tenant;
  }

  @Builder
  private static class MockWarning extends Warning {

    @Getter
    private final Instant created;
    @Getter
    private final Instant expiresAt;
    @Getter
    private final Instant revokedAt;
    @Getter
    private final String reason;
    @Getter
    private final User source;
    @Getter
    private final User revoker;
    @Getter
    private final Tenant tenant;
  }
}
