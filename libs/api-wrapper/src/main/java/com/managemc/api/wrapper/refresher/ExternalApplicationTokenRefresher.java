package com.managemc.api.wrapper.refresher;

import com.managemc.api.ApiException;
import com.managemc.api.auth.HttpBearerAuth;
import com.managemc.api.wrapper.ClientProvider;
import com.managemc.api.wrapper.client.ApiHost;
import com.managemc.api.wrapper.model.Keys;
import com.managemc.api.wrapper.model.metadata.AuthMetadataType;
import com.managemc.api.wrapper.model.metadata.ExternalApplicationAuthMetadata;
import org.openapitools.client.model.GenerateExternalServiceTokenInput;

public class ExternalApplicationTokenRefresher extends TokenRefresher<ExternalApplicationAuthMetadata> {

  private final Keys keys;

  ExternalApplicationTokenRefresher(ClientProvider.Logger logger, ApiHost apiHost, Keys keys) {
    super(logger, apiHost);

    this.keys = keys;
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
        .privateKey(keys.getPrivateKey());

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
    this.authMetadata = tokenToSerializedObject(token, ExternalApplicationAuthMetadata.class);
  }
}
