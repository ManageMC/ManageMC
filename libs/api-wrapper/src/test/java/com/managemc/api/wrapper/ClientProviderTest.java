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
        () -> newProvider(INTERNAL_PUBLIC_KEY, "oops", null).internal().getPingApi().ping()
    );

    Mockito.verify(logger, Mockito.times(1)).logWarning(TokenRefresher.BAD_CREDS_MESSAGE);
  }

  @Test
  public void externalApplication_badCredentials_shouldThrowError() {
    Assert.assertThrows(
        TokenRefresher.BadCredentialsException.class,
        () -> newProvider(EXTERNAL_PUBLIC_KEY, "oops", null).externalApplication().getPingApi().ping()
    );

    Mockito.verify(logger, Mockito.times(1)).logWarning(TokenRefresher.BAD_CREDS_MESSAGE);
  }

  @Test
  public void externalServer_badCredentials_shouldThrowError() {
    Assert.assertThrows(
        TokenRefresher.BadCredentialsException.class,
        () -> newProvider(EXTERNAL_PUBLIC_KEY, "oops", SERVER_GROUP).externalServer().getPingApi().ping()
    );

    Mockito.verify(logger, Mockito.times(1)).logWarning(TokenRefresher.BAD_CREDS_MESSAGE);
  }

  @Test
  public void internal_firstCallGeneratesMetadata() throws ApiException {
    InternalClient client = newProvider(INTERNAL_PUBLIC_KEY, INTERNAL_PRIVATE_KEY, null).internal();

    double before = currentTimeSeconds() - 1;

    Assert.assertNull(client.getAuthMetadata());
    Assert.assertEquals("pong!", client.getPingApi().ping().getMessage());

    double after = currentTimeSeconds() + 1;

    InternalAuthMetadata authMetadata = client.getAuthMetadata();
    Assert.assertEquals("internal_minecraft", authMetadata.getType().getType());
    Assert.assertTrue(authMetadata.getIat() >= before);
    Assert.assertTrue(authMetadata.getIat() <= after);
    Assert.assertTrue(authMetadata.getExp() > after);
  }

  @Test
  public void externalServer_firstCallGeneratesMetadata() throws ApiException {
    ExternalServerClient client = newProvider(EXTERNAL_PUBLIC_KEY, EXTERNAL_PRIVATE_KEY, SERVER_GROUP).externalServer();

    double before = currentTimeSeconds() - 1;

    Assert.assertNull(client.getAuthMetadata());
    Assert.assertEquals("pong!", client.getPingApi().ping().getMessage());

    double after = currentTimeSeconds() + 1;

    ExternalServerAuthMetadata authMetadata = client.getAuthMetadata();

    Assert.assertEquals("external_minecraft", authMetadata.getType().getType());
    Assert.assertTrue(authMetadata.getIat() >= before);
    Assert.assertTrue(authMetadata.getIat() <= after);
    Assert.assertTrue(authMetadata.getExp() > after);
    Assert.assertEquals(SERVER_NETWORK_ID, authMetadata.getServerNetworkId());
    Assert.assertEquals(SERVER_GROUP_ID, authMetadata.getServerGroupId());
  }

  @Test
  public void externalApplication_firstCallGeneratesMetadata() throws ApiException {
    ExternalApplicationClient client = newProvider(EXTERNAL_PUBLIC_KEY, EXTERNAL_PRIVATE_KEY, null).externalApplication();

    double before = currentTimeSeconds() - 1;

    Assert.assertNull(client.getAuthMetadata());
    Assert.assertEquals("pong!", client.getPingApi().ping().getMessage());

    double after = currentTimeSeconds() + 1;

    ExternalApplicationAuthMetadata authMetadata = client.getAuthMetadata();
    Assert.assertEquals("external_minecraft", authMetadata.getType().getType());
    Assert.assertTrue(authMetadata.getIat() >= before);
    Assert.assertTrue(authMetadata.getIat() <= after);
    Assert.assertTrue(authMetadata.getExp() > after);
    Assert.assertEquals(SERVER_NETWORK_ID, authMetadata.getServerNetworkId());
  }

  @Test
  public void player_firstCallGeneratesMetadata() throws ApiException {
    PlayerClient client = newProvider(EXTERNAL_PUBLIC_KEY, EXTERNAL_PRIVATE_KEY, SERVER_GROUP).player(OWNER_UUID);

    double before = currentTimeSeconds() - 1;

    Assert.assertNull(client.getAuthMetadata());
    Assert.assertEquals("pong!", client.getPingApi().ping().getMessage());

    double after = currentTimeSeconds() + 1;

    PlayerAuthMetadata authMetadata = client.getAuthMetadata();
    Assert.assertEquals("user_minecraft", authMetadata.getType().getType());
    Assert.assertTrue(authMetadata.getIat() >= before);
    Assert.assertTrue(authMetadata.getIat() <= after);
    Assert.assertTrue(authMetadata.getExp() > after);
    Assert.assertEquals(SERVER_NETWORK_ID, authMetadata.getServerNetworkId());
    Assert.assertEquals(SERVER_GROUP_ID, authMetadata.getServerGroupId());
    Assert.assertNotNull(authMetadata.getPlayer());
    Assert.assertEquals(1, authMetadata.getPlayer().getId());
    Assert.assertEquals("hclewk", authMetadata.getPlayer().getUsername());
    Assert.assertEquals(OWNER_UUID, authMetadata.getPlayer().getUuid());
  }


  private ClientProvider newProvider(String publicKey, String privateKey, String serverGroup) {
    return ClientProvider.demo(logger, new Keys(publicKey, privateKey), serverGroup);
  }

  // we have to floor to a whole 1000, because the API returns a value in seconds (per JWT standard)
  private double currentTimeSeconds() {
    return System.currentTimeMillis() / (double) 1000;
  }
}

