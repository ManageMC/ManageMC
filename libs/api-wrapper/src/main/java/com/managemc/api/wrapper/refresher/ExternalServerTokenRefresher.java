package com.managemc.api.wrapper.refresher;

import com.managemc.api.ApiException;
import com.managemc.api.auth.HttpBearerAuth;
import com.managemc.api.wrapper.ClientProvider;
import com.managemc.api.wrapper.client.ApiHost;
import com.managemc.api.wrapper.model.Keys;
import com.managemc.api.wrapper.model.metadata.ExternalServerAuthMetadata;
import com.managemc.api.wrapper.model.metadata.AuthMetadataType;
import org.openapitools.client.model.GenerateExternalServiceTokenInput;

public class ExternalServerTokenRefresher extends TokenRefresher<ExternalServerAuthMetadata> {

  private final Keys keys;
  private final String serverGroup;

  ExternalServerTokenRefresher(ClientProvider.Logger logger, ApiHost apiHost, Keys keys, String serverGroup) {
    super(logger, apiHost);

    this.keys = keys;
    this.serverGroup = serverGroup;
  }

  @Override
  void refreshToken() throws ApiException {
    String token = fetchNewToken();
    setToken(token);
    setAuthMetadataFromToken(token);
  }

  private String fetchNewToken() throws ApiException {
    GenerateExternalServiceTokenInput input = new GenerateExternalServiceTokenInput()
        .publicKey(keys.getPublicKey())
        .privateKey(keys.getPrivateKey())
        .serverGroup(serverGroup);

    String token = unproxiedAuthApi.generateExternalServiceToken(input).getToken();
    if (token == null) {
      throw new BadCredentialsException();
    }

    return token;
  }

  private void setToken(String token) {
    HttpBearerAuth auth = (HttpBearerAuth) client
        .getAuthentication(AuthMetadataType.EXTERNAL.getType());
    auth.setBearerToken(token);
  }

  private void setAuthMetadataFromToken(String token) {
    this.authMetadata = tokenToSerializedObject(token, ExternalServerAuthMetadata.class);
  }
}
