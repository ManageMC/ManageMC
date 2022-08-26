package com.managemc.importer.command;

import com.managemc.api.ApiException;
import com.managemc.importer.TestsWithServiceClient;
import com.managemc.importer.config.ManageMCImportPluginConfig;
import com.managemc.importer.data.ImportJobTracker;
import com.managemc.importer.reader.source.AdvancedBanDb;
import com.managemc.importer.reader.source.MaxBansPlusDb;
import com.managemc.importer.service.OnboardingApiService;
import com.managemc.importer.service.PunishmentImporterService;
import com.managemc.importer.translation.AdvancedBanTypeTranslator;
import com.managemc.plugins.logging.BukkitLogging;
import lombok.Getter;
import me.leoko.advancedban.utils.Punishment;
import me.leoko.advancedban.utils.PunishmentType;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openapitools.client.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class CmdImportAdvancedBanTest extends TestsWithServiceClient {

  private static final long IMPORT_ID = 905;

  private String advancedBanMainClass;

  private String name;
  private String uuid;
  private String reason;
  private String operator;
  private PunishmentType type;
  private long start;

  private Long expectedDuration;
  private UUID expectedOffenderUuid;
  private boolean shouldBePardoned;
  private boolean shouldHaveIssuer;

  private List<Punishment> punishments;
  private List<Punishment> punishmentHistory;

  @Mock
  private BukkitLogging logging;
  @Mock
  private ManageMCImportPluginConfig config;
  @Mock
  private OnboardingApiService onboardingApiService;
  @Mock
  private CommandSender sender;
  @Mock
  private AdvancedBanDb db;
  @Mock
  private ImportJobTracker jobTracker;

  @Before
  public void setup() throws ApiException {
    advancedBanMainClass = "me.leoko.advancedban.bukkit.BukkitMain";

    name = "AuntPhyllis";
    uuid = "c61ac8c2-59bd-46e7-8ee2-2a1273cbe07a";
    reason = "cheating";
    operator = "JacobCrofts";
    type = PunishmentType.BAN;
    start = 1600000000000L;

    expectedDuration = null;
    expectedOffenderUuid = UUID.fromString(uuid);
    shouldBePardoned = false;
    shouldHaveIssuer = true;

    punishments = new ArrayList<>();
    punishmentHistory = new ArrayList<>();

    Mockito.when(db.getPunishments()).thenReturn(punishments);
    Mockito.when(db.getPunishmentHistory()).thenReturn(punishmentHistory);

    Mockito.when(onboardingApiService.createImport()).thenReturn(IMPORT_ID);
  }

  @Test
  public void mainClassNotFound() {
    advancedBanMainClass = "com.oops.notreal";
    onCommand("advanced_ban");
    Mockito.verify(sender).sendMessage(ArgumentMatchers.contains(ChatColor.RED +
        "Unable to find the AdvancedBan plugin running locally"));
    Mockito.verify(sender, Mockito.times(1)).sendMessage((String) ArgumentMatchers.any());
  }

  @Test
  public void noPunishments() {
    Mockito.when(db.getPunishments()).thenReturn(Collections.emptyList());
    Mockito.when(db.getPunishmentHistory()).thenReturn(Collections.emptyList());

    awaitAsyncCommand(() -> onCommand("ADVANCED_BAN"));
    Mockito.verify(sender).sendMessage(ArgumentMatchers.contains("Punishment import job has begun with ID"));
    Mockito.verify(sender).sendMessage(PunishmentImporterService.READING_MESSAGE);
    Mockito.verify(sender).sendMessage(PunishmentImporterService.NO_PUNISHMENTS_FOUND_MESSAGE);
    Mockito.verify(sender, Mockito.times(3)).sendMessage((String) ArgumentMatchers.any());

    Mockito.verifyNoInteractions(onboardingApiService);
  }

  /**
   * ------------------------------
   * AdvancedBan import test cases:
   * ------------------------------
   * - should ignore certain punishment types (notes and kicks)
   * - "end" field should be ignored on punishments where the type is considered permanent
   * - if "CONSOLE" issued the punishment, issuer attributes are blank
   * - if "operator" is not a valid username, issuer attributes are blank
   * - if "operator" is null, issuer should be null
   * - if "reason" is null, reason should be blank
   * - if "name" is null, username should be blank
   * - punishments not in the "active" punishments table are considered to have been pardoned
   * - if UUID is not hyphenated in the db, it will be hyphenated by the plugin
   * - "temporary" warnings are considered permanent
   * - all warnings are assumed to be ban warnings
   */

  @Test
  public void ban_permanent() throws ApiException {
    punishments.add(buildPunishment());
    punishmentHistory.add(buildPunishment());

    awaitAsyncCommand(() -> onCommand("ADVANCED_BAN"));
    assertThatBanWasImported();
  }

  @Test
  public void ban_temp() throws ApiException {
    type = PunishmentType.TEMP_BAN;
    expectedDuration = 500_000L;

    punishments.add(buildPunishment());
    punishmentHistory.add(buildPunishment());

    awaitAsyncCommand(() -> onCommand("ADVANCED_BAN"));
    assertThatBanWasImported();
  }

  @Test
  public void ban_pardoned() throws ApiException {
    punishmentHistory.add(buildPunishment());

    shouldBePardoned = true;

    awaitAsyncCommand(() -> onCommand("ADVANCED_BAN"));
    assertThatBanWasImported();
  }

  @Test
  public void ban_issuedByConsoleUser() throws ApiException {
    operator = "CONSOLE";
    shouldHaveIssuer = false;

    punishments.add(buildPunishment());
    punishmentHistory.add(buildPunishment());

    awaitAsyncCommand(() -> onCommand("ADVANCED_BAN"));
    assertThatBanWasImported();
  }

  @Test
  public void ban_invalidOperatorUsername() throws ApiException {
    operator = "uh... oops";
    shouldHaveIssuer = false;

    punishments.add(buildPunishment());
    punishmentHistory.add(buildPunishment());

    awaitAsyncCommand(() -> onCommand("ADVANCED_BAN"));
    assertThatBanWasImported();
  }

  @Test
  public void ban_nullableAttributes() throws ApiException {
    name = null;
    reason = null;
    operator = null;
    shouldHaveIssuer = false;

    punishments.add(buildPunishment());
    punishmentHistory.add(buildPunishment());

    awaitAsyncCommand(() -> onCommand("ADVANCED_BAN"));
    assertThatBanWasImported();
  }

  @Test
  public void ban_unhyphenatedUuid() throws ApiException {
    uuid = "c61ac8c259bd46e78ee22a1273cbe07a";
    expectedOffenderUuid = UUID.fromString("c61ac8c2-59bd-46e7-8ee2-2a1273cbe07a");

    punishments.add(buildPunishment());
    punishmentHistory.add(buildPunishment());

    awaitAsyncCommand(() -> onCommand("ADVANCED_BAN"));
    assertThatBanWasImported();
  }

  @Test
  public void mute_permanent() throws ApiException {
    type = PunishmentType.MUTE;

    punishments.add(buildPunishment());
    punishmentHistory.add(buildPunishment());

    awaitAsyncCommand(() -> onCommand("ADVANCED_BAN"));
    assertThatMuteWasImported();
  }

  @Test
  public void mute_temp() throws ApiException {
    type = PunishmentType.TEMP_MUTE;
    expectedDuration = 500_000L;

    punishments.add(buildPunishment());
    punishmentHistory.add(buildPunishment());

    awaitAsyncCommand(() -> onCommand("ADVANCED_BAN"));
    assertThatMuteWasImported();
  }

  @Test
  public void mute_pardoned() throws ApiException {
    type = PunishmentType.MUTE;
    punishmentHistory.add(buildPunishment());

    shouldBePardoned = true;

    awaitAsyncCommand(() -> onCommand("ADVANCED_BAN"));
    assertThatMuteWasImported();
  }

  @Test
  public void mute_issuedByConsoleUser() throws ApiException {
    type = PunishmentType.MUTE;
    operator = "CONSOLE";
    shouldHaveIssuer = false;

    punishments.add(buildPunishment());
    punishmentHistory.add(buildPunishment());

    awaitAsyncCommand(() -> onCommand("ADVANCED_BAN"));
    assertThatMuteWasImported();
  }

  @Test
  public void mute_invalidOperatorUsername() throws ApiException {
    type = PunishmentType.MUTE;
    operator = "uh... oops";
    shouldHaveIssuer = false;

    punishments.add(buildPunishment());
    punishmentHistory.add(buildPunishment());

    awaitAsyncCommand(() -> onCommand("ADVANCED_BAN"));
    assertThatMuteWasImported();
  }

  @Test
  public void mute_nullableAttributes() throws ApiException {
    type = PunishmentType.MUTE;
    name = null;
    reason = null;
    operator = null;
    shouldHaveIssuer = false;

    punishments.add(buildPunishment());
    punishmentHistory.add(buildPunishment());

    awaitAsyncCommand(() -> onCommand("ADVANCED_BAN"));
    assertThatMuteWasImported();
  }

  @Test
  public void mute_unhyphenatedUuid() throws ApiException {
    type = PunishmentType.MUTE;
    uuid = "c61ac8c259bd46e78ee22a1273cbe07a";
    expectedOffenderUuid = UUID.fromString("c61ac8c2-59bd-46e7-8ee2-2a1273cbe07a");

    punishments.add(buildPunishment());
    punishmentHistory.add(buildPunishment());

    awaitAsyncCommand(() -> onCommand("ADVANCED_BAN"));
    assertThatMuteWasImported();
  }

  @Test
  public void warning_permanent() throws ApiException {
    type = PunishmentType.WARNING;
    punishments.add(buildPunishment());
    punishmentHistory.add(buildPunishment());

    awaitAsyncCommand(() -> onCommand("ADVANCED_BAN"));
    assertThatWarningWasImported();
  }

  @Test
  public void warning_temp() throws ApiException {
    // special case: "temporary" warnings should be considered permanent
    type = PunishmentType.TEMP_WARNING;
    expectedDuration = null;

    punishments.add(buildPunishment());
    punishmentHistory.add(buildPunishment());

    awaitAsyncCommand(() -> onCommand("ADVANCED_BAN"));
    assertThatWarningWasImported();
  }

  @Test
  public void warning_pardoned() throws ApiException {
    type = PunishmentType.WARNING;
    punishmentHistory.add(buildPunishment());

    shouldBePardoned = true;

    awaitAsyncCommand(() -> onCommand("ADVANCED_BAN"));
    assertThatWarningWasImported();
  }

  @Test
  public void warning_issuedByConsoleUser() throws ApiException {
    type = PunishmentType.WARNING;
    operator = "CONSOLE";
    shouldHaveIssuer = false;

    punishments.add(buildPunishment());
    punishmentHistory.add(buildPunishment());

    awaitAsyncCommand(() -> onCommand("ADVANCED_BAN"));
    assertThatWarningWasImported();
  }

  @Test
  public void warning_invalidOperatorUsername() throws ApiException {
    type = PunishmentType.WARNING;
    operator = "uh... oops";
    shouldHaveIssuer = false;

    punishments.add(buildPunishment());
    punishmentHistory.add(buildPunishment());

    awaitAsyncCommand(() -> onCommand("ADVANCED_BAN"));
    assertThatWarningWasImported();
  }

  @Test
  public void warning_nullableAttributes() throws ApiException {
    type = PunishmentType.WARNING;
    name = null;
    reason = null;
    operator = null;
    shouldHaveIssuer = false;

    punishments.add(buildPunishment());
    punishmentHistory.add(buildPunishment());

    awaitAsyncCommand(() -> onCommand("ADVANCED_BAN"));
    assertThatWarningWasImported();
  }

  @Test
  public void warning_unhyphenatedUuid() throws ApiException {
    type = PunishmentType.WARNING;
    uuid = "c61ac8c259bd46e78ee22a1273cbe07a";
    expectedOffenderUuid = UUID.fromString("c61ac8c2-59bd-46e7-8ee2-2a1273cbe07a");

    punishments.add(buildPunishment());
    punishmentHistory.add(buildPunishment());

    awaitAsyncCommand(() -> onCommand("ADVANCED_BAN"));
    assertThatWarningWasImported();
  }

  @Test
  public void ipBan_permanent() throws ApiException {
    type = PunishmentType.IP_BAN;
    name = "1.2.3.4";
    punishments.add(buildPunishment());
    punishmentHistory.add(buildPunishment());

    awaitAsyncCommand(() -> onCommand("ADVANCED_BAN"));
    assertThatIpBanWasImported();
  }

  @Test
  public void ipBan_temp() throws ApiException {
    type = PunishmentType.TEMP_IP_BAN;
    name = "1.2.3.4";
    expectedDuration = 500_000L;

    punishments.add(buildPunishment());
    punishmentHistory.add(buildPunishment());

    awaitAsyncCommand(() -> onCommand("ADVANCED_BAN"));
    assertThatIpBanWasImported();
  }

  @Test
  public void ipBan_pardoned() throws ApiException {
    type = PunishmentType.IP_BAN;
    name = "1.2.3.4";
    punishmentHistory.add(buildPunishment());

    shouldBePardoned = true;

    awaitAsyncCommand(() -> onCommand("ADVANCED_BAN"));
    assertThatIpBanWasImported();
  }

  @Test
  public void ipBan_issuedByConsoleUser() throws ApiException {
    type = PunishmentType.IP_BAN;
    name = "1.2.3.4";
    operator = "CONSOLE";
    shouldHaveIssuer = false;

    punishments.add(buildPunishment());
    punishmentHistory.add(buildPunishment());

    awaitAsyncCommand(() -> onCommand("ADVANCED_BAN"));
    assertThatIpBanWasImported();
  }

  @Test
  public void ipBan_invalidOperatorUsername() throws ApiException {
    type = PunishmentType.IP_BAN;
    name = "1.2.3.4";
    operator = "uh... oops";
    shouldHaveIssuer = false;

    punishments.add(buildPunishment());
    punishmentHistory.add(buildPunishment());

    awaitAsyncCommand(() -> onCommand("ADVANCED_BAN"));
    assertThatIpBanWasImported();
  }

  @Test
  public void ipBan_nullableAttributes() throws ApiException {
    type = PunishmentType.IP_BAN;
    name = "1.2.3.4";
    uuid = null;
    reason = null;
    operator = null;
    shouldHaveIssuer = false;

    punishments.add(buildPunishment());
    punishmentHistory.add(buildPunishment());

    awaitAsyncCommand(() -> onCommand("ADVANCED_BAN"));
    assertThatIpBanWasImported();
  }

  @Test
  public void shouldIgnoreNotesAndKicksForNow() {
    type = PunishmentType.NOTE;

    punishments.add(buildPunishment());
    punishmentHistory.add(buildPunishment());

    type = PunishmentType.KICK;

    punishments.add(buildPunishment());
    punishmentHistory.add(buildPunishment());

    awaitAsyncCommand(() -> onCommand("ADVANCED_BAN"));

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

    punishments.add(buildPunishment());
    punishmentHistory.add(buildPunishment());
    awaitAsyncCommand(() -> onCommand("ADVANCED_BAN"));

    assertThatImportFailed();

    Mockito.verify(onboardingApiService).createImport();
    Mockito.verifyNoMoreInteractions(onboardingApiService);
  }

  @Test
  public void failedToImportPunishments() throws Exception {
    Mockito.doThrow(new ApiException())
        .when(onboardingApiService).importBans(Mockito.eq(IMPORT_ID), Mockito.any());

    punishments.add(buildPunishment());
    punishmentHistory.add(buildPunishment());
    awaitAsyncCommand(() -> onCommand("ADVANCED_BAN"));

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

    Mockito.verify(onboardingApiService).completeImport(IMPORT_ID);

    Mockito.verify(jobTracker).trackJob(Mockito.any());
  }

  private void assertThatMuteWasImported() throws ApiException {
    assertThatSuccessMessagesWereSent();

    ImportableMutes expected = new ImportableMutes()
        .mutes(Collections.singletonList(expectedBanOrMute()));

    Mockito.verify(onboardingApiService, Mockito.never()).importBans(Mockito.anyLong(), Mockito.any());
    Mockito.verify(onboardingApiService).importMutes(IMPORT_ID, expected);
    Mockito.verify(onboardingApiService, Mockito.never()).importWarnings(Mockito.anyLong(), Mockito.any());
    Mockito.verify(onboardingApiService, Mockito.never()).importIpBans(Mockito.anyLong(), Mockito.any());

    Mockito.verify(onboardingApiService).completeImport(IMPORT_ID);

    Mockito.verify(jobTracker).trackJob(Mockito.any());
  }

  private void assertThatWarningWasImported() throws ApiException {
    assertThatSuccessMessagesWereSent();

    ImportableWarnings expected = new ImportableWarnings()
        .warnings(Collections.singletonList(expectedWarning()));

    Mockito.verify(onboardingApiService, Mockito.never()).importBans(Mockito.anyLong(), Mockito.any());
    Mockito.verify(onboardingApiService, Mockito.never()).importMutes(Mockito.anyLong(), Mockito.any());
    Mockito.verify(onboardingApiService).importWarnings(IMPORT_ID, expected);
    Mockito.verify(onboardingApiService, Mockito.never()).importIpBans(Mockito.anyLong(), Mockito.any());

    Mockito.verify(onboardingApiService).completeImport(IMPORT_ID);

    Mockito.verify(jobTracker).trackJob(Mockito.any());
  }

  private void assertThatIpBanWasImported() throws ApiException {
    assertThatSuccessMessagesWereSent();

    ImportableIpBans expected = new ImportableIpBans()
        .ipBans(Collections.singletonList(expectedIpBan()));

    Mockito.verify(onboardingApiService, Mockito.never()).importBans(Mockito.anyLong(), Mockito.any());
    Mockito.verify(onboardingApiService, Mockito.never()).importMutes(Mockito.anyLong(), Mockito.any());
    Mockito.verify(onboardingApiService, Mockito.never()).importWarnings(Mockito.anyLong(), Mockito.any());
    Mockito.verify(onboardingApiService).importIpBans(IMPORT_ID, expected);

    Mockito.verify(onboardingApiService).completeImport(IMPORT_ID);

    Mockito.verify(jobTracker).trackJob(Mockito.any());
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
        .durationMillis(expectedDuration)
        .issuedAtMillis(start)
        .pardon(expectedPardon())
        .source(AdvancedBanTypeTranslator.ADVANCEDBAN_SOURCE);
  }

  private ImportableWarning expectedWarning() {
    return new ImportableWarning()
        .offender(expectedOffender())
        .issuer(expectedIssuer())
        .reason(reason)
        .details(null)
        .issuedAtMillis(start)
        .pardon(expectedPardon())
        .source(AdvancedBanTypeTranslator.ADVANCEDBAN_SOURCE);
  }

  private ImportableIpBan expectedIpBan() {
    return new ImportableIpBan()
        .minIpv4Address(name)
        .maxIpv4Address(name)
        .issuer(expectedIssuer())
        .visibility(PunishmentVisibility.PUBLIC)
        .scope(null)
        .reason(reason)
        .details(null)
        .durationMillis(expectedDuration)
        .issuedAtMillis(start)
        .pardon(expectedPardon())
        .source(AdvancedBanTypeTranslator.ADVANCEDBAN_SOURCE);
  }

  private void assertThatSuccessMessagesWereSent() {
    Mockito.verify(sender).sendMessage(ArgumentMatchers.contains("Punishment import job has begun with ID"));
    Mockito.verify(sender).sendMessage(PunishmentImporterService.READING_MESSAGE);
    Mockito.verify(sender).sendMessage(PunishmentImporterService.IMPORTING_MESSAGE);
    Mockito.verify(sender).sendMessage(PunishmentImporterService.FINISHED_MESSAGE);
    Mockito.verify(sender, Mockito.times(4)).sendMessage((String) ArgumentMatchers.any());
  }

  private ImportablePunishmentPlayer expectedIssuer() {
    return shouldHaveIssuer
        ? new ImportablePunishmentPlayer().username(operator).uuid(null)
        : null;
  }

  private ImportablePunishmentPlayer expectedOffender() {
    return new ImportablePunishmentPlayer()
        .username(name)
        .uuid(expectedOffenderUuid);
  }

  private ImportablePunishmentPardon expectedPardon() {
    if (shouldBePardoned) {
      return new ImportablePunishmentPardon()
          .pardoner(null)
          .pardonedAtMillis(start)
          .details(AdvancedBanTypeTranslator.PARDON_DETAILS);
    }
    return null;
  }


  private Punishment buildPunishment() {
    // it doesn't matter than the "end" field is always assigned; we should be using the
    // "type" field to check whether a ban is meant to be temporary
    return new MockPunishment(name, uuid, reason, operator, type, start, start + 500_000);
  }

  private void onCommand(String... args) {
    PunishmentImporterService service = new PunishmentImporterService(
        logging,
        db,
        Mockito.mock(MaxBansPlusDb.class),
        onboardingApiService,
        jobTracker
    );

    Mockito.when(config.getImporterService()).thenReturn(service);
    Mockito.when(config.getAdvancedBanMainClass()).thenReturn(advancedBanMainClass);

    new CmdImport(logging, config).execute(sender, "nobodycares", args);
  }

  private static class MockPunishment extends Punishment {

    // these getters quietly override superclass methods
    @Getter
    private final String name;
    @Getter
    private final String uuid;
    @Getter
    private final String reason;
    @Getter
    private final String operator;
    @Getter
    private final PunishmentType type;
    @Getter
    private final long start;
    @Getter
    private final long end;

    public MockPunishment(String name, String uuid, String reason, String operator, PunishmentType type, long start, long end) {
      super(null, null, null, null, null, 0, 0, null, 0);
      this.name = name;
      this.uuid = uuid;
      this.reason = reason;
      this.operator = operator;
      this.type = type;
      this.start = start;
      this.end = end;
    }
  }
}
