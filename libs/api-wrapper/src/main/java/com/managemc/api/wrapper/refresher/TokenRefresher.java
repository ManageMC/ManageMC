package com.managemc.api.wrapper.refresher;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.managemc.api.ApiClient;
import com.managemc.api.ApiException;
import com.managemc.api.api.AuthenticationApi;
import com.managemc.api.wrapper.ClientProvider;
import com.managemc.api.wrapper.client.ApiHost;
import com.managemc.api.wrapper.model.metadata.AuthMetadata;
import javassist.util.proxy.MethodHandler;
import lombok.Getter;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.Map;

public abstract class TokenRefresher<T extends AuthMetadata> implements MethodHandler {

  private static final long THIRTY_SECONDS = 30 * 1000;
  public static final String BAD_CREDS_MESSAGE = "Invalid public/private key configured for application";
  private static final String BAD_REQUEST_MESSAGE = "A request to ManageMC's API returned a non-success status. Response body: %s";

  @Getter
  protected T authMetadata;

  protected final AuthenticationApi unproxiedAuthApi;
  @Getter
  protected final ApiClient client;
  private final ClientProvider.Logger logger;

  public TokenRefresher(ClientProvider.Logger logger, ApiHost apiHost) {
    this.client = new ApiClient()
        .setServerIndex(apiHost.getServerIndex());
    this.unproxiedAuthApi = new AuthenticationApi(client);
    this.logger = logger;
  }

  @Override
  public final Object invoke(Object o, Method thisMethod, Method proceed, Object[] args) throws Throwable {
    try {
      refreshTokenIfNecessary(thisMethod.getName());
      return proceed.invoke(o, args);
    } catch (InvocationTargetException e) {
      if (e.getCause() instanceof ApiException) {
        String body = ((ApiException) e.getCause()).getResponseBody();
        logger.logWarning(String.format(BAD_REQUEST_MESSAGE, body));
      }
      throw e.getCause();
    } catch (BadCredentialsException e) {
      logger.logWarning(e.getMessage());
      throw e;
    } finally {
      // helps with testing by making it easy for us to tell if an async request has finished
      // if this behaves unexpectedly, possible causes include a method we are filtering incorrectly
      ClientProvider.COMPLETED_REQUESTS.addAndGet(1);
    }
  }

  /**
   * Should refresh the token if the token is expired. Failure to refresh the token
   * should result in a runtime exception.
   */
  private void refreshTokenIfNecessary(String methodName) throws ApiException {
    // TODO: remove this once we're confident we found everything (see TokenRefresherMethodFilter)
    logger.logInfo("Request made to ManageMC API: " + methodName);

    if (shouldRefresh()) {
      refreshToken();
    }
  }

  abstract void refreshToken() throws ApiException;

  protected Map<String, Object> tokenToJsonMap(String token) {
    String payload = token.split("\\.")[1];
    byte[] decodedPayloadBytes = Base64.getDecoder().decode(payload);
    String decodedPayload = new String(decodedPayloadBytes);

    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.readValue(decodedPayload, JSON_MAP_TYPE);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private boolean shouldRefresh() {
    if (authMetadata == null) {
      return true;
    }

    long thirtySecondsFromNow = System.currentTimeMillis() + THIRTY_SECONDS;
    return thirtySecondsFromNow > authMetadata.getExpiresAtMillis();
  }

  public static class BadCredentialsException extends RuntimeException {
    public BadCredentialsException() {
      super(BAD_CREDS_MESSAGE);
    }
  }

  private static final TypeReference<Map<String, Object>> JSON_MAP_TYPE =
      new TypeReference<Map<String, Object>>() {
      };
}
