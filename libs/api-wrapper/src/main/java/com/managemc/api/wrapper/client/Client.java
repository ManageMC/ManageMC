package com.managemc.api.wrapper.client;

import com.managemc.api.ApiClient;
import com.managemc.api.api.*;
import com.managemc.api.wrapper.model.metadata.AuthMetadata;
import com.managemc.api.wrapper.refresher.TokenRefresher;
import com.managemc.api.wrapper.refresher.filter.TokenRefresherMethodFilter;
import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.ProxyFactory;
import lombok.Getter;

import java.lang.reflect.InvocationHandler;

public abstract class Client<T extends AuthMetadata> {
  @Getter
  private final ApiClient client;
  @Getter
  private final PingApi pingApi;
  @Getter
  private final AuthenticationApi authenticationApi;
  @Getter
  private final AccountsApi accountsApi;
  @Getter
  private final ReportsApi reportsApi;
  @Getter
  private final HeartbeatsApi heartbeatsApi;
  @Getter
  private final NotesApi notesApi;
  @Getter
  private final OnboardingApi onboardingApi;
  @Getter
  private final PlayersApi playersApi;
  @Getter
  private final PunishmentsApi punishmentsApi;
  @Getter
  private final ServerNetworksApi serverNetworksApi;
  @Getter
  private final ServerTrafficApi serverTrafficApi;
  @Getter
  private final ServerGroupsApi serverGroupsApi;

  private final TokenRefresher<T> refresher;

  public Client(TokenRefresher<T> refresher) {
    this.client = refresher.getClient();
    this.refresher = refresher;
    this.pingApi = generateProxyApi(PingApi.class);
    this.authenticationApi = generateProxyApi(AuthenticationApi.class);
    this.accountsApi = generateProxyApi(AccountsApi.class);
    this.reportsApi = generateProxyApi(ReportsApi.class);
    this.heartbeatsApi = generateProxyApi(HeartbeatsApi.class);
    this.notesApi = generateProxyApi(NotesApi.class);
    this.onboardingApi = generateProxyApi(OnboardingApi.class);
    this.playersApi = generateProxyApi(PlayersApi.class);
    this.punishmentsApi = generateProxyApi(PunishmentsApi.class);
    this.serverNetworksApi = generateProxyApi(ServerNetworksApi.class);
    this.serverTrafficApi = generateProxyApi(ServerTrafficApi.class);
    this.serverGroupsApi = generateProxyApi(ServerGroupsApi.class);
  }

  public T getAuthMetadata() {
    return refresher.getAuthMetadata();
  }

  private static final MethodFilter METHOD_FILTER = new TokenRefresherMethodFilter();
  private static final Class<?>[] ARG_TYPES = new Class<?>[]{ApiClient.class};

  /**
   * Java's {@link InvocationHandler} is what is normally used to create a "proxy" class that
   * performs preliminary actions before forwarding methods to the class it's wrapping. But
   * that only works for interfaces, and since OpenAPI generates an abstract class instead of
   * an interface, we have to use a special library that provides similar functionality for
   * abstract classes.
   * <p>
   * The wrapper we provide automatically refreshes each client's auth tokens before making API
   * requests. The {@link TokenRefresherMethodFilter} determines which methods qualify for auth
   * token refreshes. Every time we update OpenAPI generator, we need to be careful about new
   * methods that might have been added so that we don't refresh tokens redundantly.
   */
  @SuppressWarnings("unchecked")
  private <C> C generateProxyApi(Class<C> apiClass) {
    ProxyFactory factory = new ProxyFactory();
    factory.setSuperclass(apiClass);
    factory.setFilter(METHOD_FILTER);

    try {
      Object[] args = new Object[]{client};
      return (C) factory.create(ARG_TYPES, args, refresher);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
