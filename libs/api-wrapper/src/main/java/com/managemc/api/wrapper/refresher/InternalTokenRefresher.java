package com.managemc.api.wrapper.refresher;

import com.managemc.api.ApiException;
import com.managemc.api.auth.HttpBearerAuth;
import com.managemc.api.wrapper.ClientProvider;
import com.managemc.api.wrapper.client.ApiHost;
import com.managemc.api.wrapper.model.Keys;
import com.managemc.api.wrapper.model.metadata.AuthMetadataType;
import com.managemc.api.wrapper.model.metadata.InternalAuthMetadata;
import org.openapitools.client.model.GenerateInternalServiceTokenInput;

public class InternalTokenRefresher extends TokenRefresher<InternalAuthMetadata> {

  private final Keys keys;

  InternalTokenRefresher(ClientProvider.Logger logger, ApiHost apiHost, Keys keys) {
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
    GenerateInternalServiceTokenInput input = new GenerateInternalServiceTokenInput()
        .publicKey(keys.getPublicKey())
        .privateKey(keys.getPrivateKey());

    String token = unproxiedAuthApi.generateInternalServiceToken(input).getToken();
    if (token == null) {
      throw new BadCredentialsException();
    }

    return token;
  }

  private void setToken(String token) {
    HttpBearerAuth auth = (HttpBearerAuth) client
        .getAuthentication(AuthMetadataType.INTERNAL.getType());
    auth.setBearerToken(token);
  }

  private void setAuthMetadataFromToken(String token) {
    this.authMetadata = tokenToSerializedObject(token, InternalAuthMetadata.class);
  }
}
