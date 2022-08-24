package com.managemc.importer.service;

import com.managemc.api.ApiException;
import com.managemc.api.wrapper.ClientProvider;
import org.openapitools.client.model.ImportableBans;
import org.openapitools.client.model.ImportableIpBans;
import org.openapitools.client.model.ImportableMutes;
import org.openapitools.client.model.ImportableWarnings;

public class OnboardingApiService {

  private final ClientProvider clientProvider;

  public OnboardingApiService(ClientProvider clientProvider) {
    this.clientProvider = clientProvider;
  }

  public long createImport() throws ApiException {
    return clientProvider.externalApplication().getOnboardingApi()
        .initiatePunishmentImport().getId();
  }

  public void completeImport(long importId) throws ApiException {
    clientProvider.externalApplication().getOnboardingApi()
        .completePunishmentImport(importId);
  }

  public void cancelImport(long importId) throws ApiException {
    clientProvider.externalApplication().getOnboardingApi()
        .cancelPunishmentImport(importId);
  }

  public void importBans(long importId, ImportableBans bans) throws ApiException {
    clientProvider.externalApplication().getOnboardingApi().importBans(importId, bans);
  }

  public void importMutes(long importId, ImportableMutes mutes) throws ApiException {
    clientProvider.externalApplication().getOnboardingApi().importMutes(importId, mutes);
  }

  public void importWarnings(long importId, ImportableWarnings warnings) throws ApiException {
    clientProvider.externalApplication().getOnboardingApi().importWarnings(importId, warnings);
  }

  public void importIpBans(long importId, ImportableIpBans ipBans) throws ApiException {
    clientProvider.externalApplication().getOnboardingApi().importIpBans(importId, ipBans);
  }
}
