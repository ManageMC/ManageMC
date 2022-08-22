package com.managemc.api.wrapper;

import com.managemc.api.ApiException;
import com.managemc.api.wrapper.client.ExternalApplicationClient;
import com.managemc.api.wrapper.client.ExternalServerClient;
import com.managemc.api.wrapper.client.InternalClient;
import com.managemc.api.wrapper.client.PlayerClient;
import com.managemc.api.wrapper.model.Keys;
import com.managemc.api.wrapper.model.metadata.ExternalApplicationAuthMetadata;
import com.managemc.api.wrapper.model.metadata.ExternalServerAuthMetadata;
import com.managemc.api.wrapper.model.metadata.InternalAuthMetadata;
import com.managemc.api.wrapper.model.metadata.PlayerAuthMetadata;
import com.managemc.api.wrapper.refresher.TokenRefresher;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.UUID;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ClientProviderTest {

  private static final String INTERNAL_PUBLIC_KEY = "afbd5735-de32-47a1-b6a1-264f5fb57565";
  private static final String INTERNAL_PRIVATE_KEY = "wjuNCHscWLMzQYoU_4y37g";
  private static final String EXTERNAL_PUBLIC_KEY = "006d4fdd-8779-4e72-b699-eeaf53fd8203";
  private static final String EXTERNAL_PRIVATE_KEY = "zRaHN10wP4oGpNs5qf74Ig";
  private static final String SERVER_GROUP = "KitPvP";
  private static final long SERVER_NETWORK_ID = 1;
  private static final long SERVER_GROUP_ID = 1;
  private static final UUID OWNER_UUID = UUID.fromString("6c602796-ee0f-41a7-903f-d49aff33a8f4");

  @Mock
  private ClientProvider.Logger logger;

  @Test
  public void internal_badCredentials_shouldThrowError() {
    Assert.assertThrows(
        TokenRefresher.BadCredentialsException.class,
        () -> ClientProvider.local(logger, new Keys(INTERNAL_PUBLIC_KEY, "oops"), null).internal().getPingApi().ping()
    );

    Mockito.verify(logger, Mockito.times(1)).logWarning(TokenRefresher.BAD_CREDS_MESSAGE);
  }

  @Test
  public void externalApplication_badCredentials_shouldThrowError() {
    Assert.assertThrows(
        TokenRefresher.BadCredentialsException.class,
        () -> ClientProvider.local(logger, new Keys(EXTERNAL_PUBLIC_KEY, "oops"), null).externalApplication().getPingApi().ping()
    );

    Mockito.verify(logger, Mockito.times(1)).logWarning(TokenRefresher.BAD_CREDS_MESSAGE);
  }

  @Test
  public void externalServer_badCredentials_shouldThrowError() {
    Assert.assertThrows(
        TokenRefresher.BadCredentialsException.class,
        () -> ClientProvider.local(logger, new Keys(EXTERNAL_PUBLIC_KEY, "oops"), SERVER_GROUP).externalServer().getPingApi().ping()
    );

    Mockito.verify(logger, Mockito.times(1)).logWarning(TokenRefresher.BAD_CREDS_MESSAGE);
  }

  @Test
  public void internal_firstCallGeneratesMetadata() throws ApiException {
    InternalClient client = ClientProvider.local(logger, new Keys(INTERNAL_PUBLIC_KEY, INTERNAL_PRIVATE_KEY), null)
        .internal();

    long before = currentTime();

    Assert.assertNull(client.getAuthMetadata());
    Assert.assertEquals("pong!", client.getPingApi().ping().getMessage());

    long after = currentTime();

    InternalAuthMetadata authMetadata = client.getAuthMetadata();
    Assert.assertEquals("internal_minecraft", authMetadata.getType().getType());
    Assert.assertTrue(authMetadata.getIssuedAtMillis() >= before);
    Assert.assertTrue(authMetadata.getIssuedAtMillis() <= after);
    Assert.assertTrue(authMetadata.getExpiresAtMillis() > after);
  }

  @Test
  public void externalServer_firstCallGeneratesMetadata() throws ApiException {
    ExternalServerClient client =
        ClientProvider.local(logger, new Keys(EXTERNAL_PUBLIC_KEY, EXTERNAL_PRIVATE_KEY), SERVER_GROUP).externalServer();

    long before = currentTime();

    Assert.assertNull(client.getAuthMetadata());
    Assert.assertEquals("pong!", client.getPingApi().ping().getMessage());

    long after = currentTime();

    ExternalServerAuthMetadata authMetadata = client.getAuthMetadata();
    Assert.assertEquals("external_minecraft", authMetadata.getType().getType());
    Assert.assertTrue(authMetadata.getIssuedAtMillis() >= before);
    Assert.assertTrue(authMetadata.getIssuedAtMillis() <= after);
    Assert.assertTrue(authMetadata.getExpiresAtMillis() > after);
    Assert.assertEquals(SERVER_NETWORK_ID, authMetadata.getServerNetworkId());
    Assert.assertEquals(SERVER_GROUP_ID, authMetadata.getServerGroupId());
  }

  @Test
  public void externalApplication_firstCallGeneratesMetadata() throws ApiException {
    ExternalApplicationClient client =
        ClientProvider.local(logger, new Keys(EXTERNAL_PUBLIC_KEY, EXTERNAL_PRIVATE_KEY), null).externalApplication();

    long before = currentTime();

    Assert.assertNull(client.getAuthMetadata());
    Assert.assertEquals("pong!", client.getPingApi().ping().getMessage());

    long after = currentTime();

    ExternalApplicationAuthMetadata authMetadata = client.getAuthMetadata();
    Assert.assertEquals("external_minecraft", authMetadata.getType().getType());
    Assert.assertTrue(authMetadata.getIssuedAtMillis() >= before);
    Assert.assertTrue(authMetadata.getIssuedAtMillis() <= after);
    Assert.assertTrue(authMetadata.getExpiresAtMillis() > after);
    Assert.assertEquals(SERVER_NETWORK_ID, authMetadata.getServerNetworkId());
  }

  @Test
  public void player_firstCallGeneratesMetadata() throws ApiException {
    PlayerClient client =
        ClientProvider.local(logger, new Keys(EXTERNAL_PUBLIC_KEY, EXTERNAL_PRIVATE_KEY), SERVER_GROUP).player(OWNER_UUID);

    long before = currentTime();

    Assert.assertNull(client.getAuthMetadata());
    Assert.assertEquals("pong!", client.getPingApi().ping().getMessage());

    long after = currentTime();

    PlayerAuthMetadata authMetadata = client.getAuthMetadata();
    Assert.assertEquals("user_minecraft", authMetadata.getType().getType());
    Assert.assertTrue(authMetadata.getIssuedAtMillis() >= before);
    Assert.assertTrue(authMetadata.getIssuedAtMillis() <= after);
    Assert.assertTrue(authMetadata.getExpiresAtMillis() > after);
    Assert.assertEquals(SERVER_NETWORK_ID, authMetadata.getServerNetworkId());
    Assert.assertEquals(SERVER_GROUP_ID, authMetadata.getServerGroupId());
    Assert.assertNotNull(authMetadata.getPlayer());
    Assert.assertEquals(1, authMetadata.getPlayer().getId());
    Assert.assertEquals("hclewk", authMetadata.getPlayer().getUsername());
    Assert.assertEquals(OWNER_UUID, authMetadata.getPlayer().getUuid());
  }


  // we have to floor to a whole 1000, because the API returns a value in seconds (per JWT standard)
  private long currentTime() {
    return System.currentTimeMillis() / 1000 * 1000;
  }
}

