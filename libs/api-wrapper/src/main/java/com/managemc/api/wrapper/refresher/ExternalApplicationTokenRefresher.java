package com.managemc.api.wrapper.refresher;

import com.managemc.api.ApiException;
import com.managemc.api.auth.HttpBearerAuth;
import com.managemc.api.wrapper.ClientProvider;
import com.managemc.api.wrapper.model.Keys;
import com.managemc.api.wrapper.model.metadata.AuthMetadataType;
import com.managemc.api.wrapper.model.metadata.ExternalApplicationAuthMetadata;
import org.openapitools.client.model.GenerateExternalServiceTokenInput;

import java.util.Map;

public class ExternalApplicationTokenRefresher extends TokenRefresher<ExternalApplicationAuthMetadata> {

  private final Keys keys;

  ExternalApplicationTokenRefresher(ClientProvider.Logger logger, String basePath, Keys keys) {
    super(logger, basePath);

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
    Map<String, Object> authMetadataMap = tokenToJsonMap(token);
    this.authMetadata = ExternalApplicationAuthMetadata.builder()
        .issuedAtMillis(Long.parseLong(authMetadataMap.get("iat").toString()) * 1000)
        .expiresAtMillis(Long.parseLong(authMetadataMap.get("exp").toString()) * 1000)
        .serverNetworkId(Long.parseLong(authMetadataMap.get("server_network_id").toString()))
        .build();
  }
}
