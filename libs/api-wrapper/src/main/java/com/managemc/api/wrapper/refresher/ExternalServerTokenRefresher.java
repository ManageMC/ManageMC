package com.managemc.api.wrapper.refresher;

import com.managemc.api.ApiException;
import com.managemc.api.auth.HttpBearerAuth;
import com.managemc.api.wrapper.ClientProvider;
import com.managemc.api.wrapper.model.Keys;
import com.managemc.api.wrapper.model.metadata.AuthMetadataType;
import com.managemc.api.wrapper.model.metadata.ExternalServerAuthMetadata;
import org.openapitools.client.model.GenerateExternalServiceTokenInput;

import java.util.Map;

public class ExternalServerTokenRefresher extends WebServiceTokenRefresher<ExternalServerAuthMetadata> {

  private final Keys keys;
  private final String serverGroup;

  ExternalServerTokenRefresher(ClientProvider.Logger logger, String basePath, Keys keys, String serverGroup) {
    super(logger, basePath);

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
    Map<String, Object> authMetadataMap = tokenToJsonMap(token);
    this.authMetadata = ExternalServerAuthMetadata.builder()
        .issuedAtMillis(Long.parseLong(authMetadataMap.get("iat").toString()) * 1000)
        .expiresAtMillis(Long.parseLong(authMetadataMap.get("exp").toString()) * 1000)
        .serverNetworkId(Long.parseLong(authMetadataMap.get("server_network_id").toString()))
        .serverGroupId(Long.parseLong(authMetadataMap.get("server_group_id").toString()))
        .build();
  }
}
