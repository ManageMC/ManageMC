package com.managemc.api.wrapper;

import com.managemc.api.wrapper.client.*;
import com.managemc.api.wrapper.model.Keys;
import com.managemc.api.wrapper.refresher.*;
import lombok.NonNull;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class ClientProvider {

  public static final AtomicLong COMPLETED_REQUESTS = new AtomicLong(0);

  private static final String LOCAL_BASE_PATH = "http://localhost:9070/api/v1";

  private final Logger logger;
  private final String basePath;
  private final Keys keys;
  private final String serverGroup;
  private final ScheduledExecutorService executorService;

  /**
   * OpenAPI generator does not currently produce thread-safe Java clients. Since these
   * clients all have the potential to make requests very quickly and from more than one
   * thread, we create a new client instance per thread that requests access.
   */
  private ThreadLocal<InternalClient> internalClient;
  private ThreadLocal<ExternalServerClient> externalServerClient;
  private ThreadLocal<ExternalApplicationClient> externalApplicationClient;

  public static ClientProvider local(@NonNull Logger logger, @NonNull Keys keys, String serverGroup) {
    return new ClientProvider(logger, LOCAL_BASE_PATH, keys, serverGroup);
  }

  /**
   * One client per player is fine for now. Players can't make requests fast enough that
   * it would make sense to have one client per player per thread. If necessary, we can
   * always add that.
   */
  private final Map<UUID, PlayerClient> playerClients = new ConcurrentHashMap<>();

  private ClientProvider(Logger logger, String basePath, Keys keys, String serverGroup) {
    this.logger = logger;
    this.basePath = basePath;
    this.keys = keys;
    this.serverGroup = serverGroup;
    this.executorService = Executors.newSingleThreadScheduledExecutor();

    // remove user/player clients that have not been used
    // important because each user/player needs a separately authenticated client,
    // and each client wraps a different OkHttpClient, which maintains a
    // connection to the web service
    this.executorService.scheduleAtFixedRate(() -> {
      long now = System.currentTimeMillis();
      Set<UUID> playerClientsToRemove = playerClients.entrySet().stream()
          .filter(entry -> entry.getValue().getAuthMetadata().getExpiresAtMillis() > now)
          .map(Map.Entry::getKey)
          .collect(Collectors.toSet());

      playerClientsToRemove.forEach(playerClients::remove);
    }, 12, 1, TimeUnit.MINUTES);
  }

  /**
   * Internal use only. This probably should be moved to our internal plugin, but we are
   * feeling lazy right now.
   */
  public InternalClient internal() {
    if (internalClient != null) {
      return internalClient.get();
    }

    InternalTokenRefresher tokenRefresher = WebServiceTokenRefreshers
        .internal(logger, basePath, keys);
    internalClient = ThreadLocal.withInitial(() -> new InternalClient(tokenRefresher));
    return internalClient.get();
  }

  /**
   * Players use a different service client with a different authentication type. If the player
   * tries to do something they don't have permission to do, then the API will throw a 403. We
   * should try to catch permission errors before making API requests, but this adds an extra
   * layer of safety for us.
   * <p>
   * In contrast, the "external" client represents the Minecraft server itself, which is far
   * less restricted.
   */
  public Client<?> forCommandSender(CommandSender sender) {
    if (sender instanceof Player) {
      return player(((Player) sender).getUniqueId());
    }
    return externalServer();
  }

  /**
   * Represents a Minecraft server that belongs to a specific server group. The API will not
   * check permissions restrictions for this client, so only OP players and console command
   * senders should be able to use it.
   */
  public ExternalServerClient externalServer() {
    if (externalServerClient != null) {
      return externalServerClient.get();
    }

    ExternalServerTokenRefresher tokenRefresher = WebServiceTokenRefreshers
        .externalServer(logger, basePath, keys, serverGroup);
    externalServerClient = ThreadLocal.withInitial(() -> new ExternalServerClient(tokenRefresher));
    return externalServerClient.get();
  }

  /**
   * Represents a customer-hosted application of any kind that does NOT belong to a server
   * group. Used for e.g. data imports.
   */
  public ExternalApplicationClient externalApplication() {
    if (externalApplicationClient != null) {
      return externalApplicationClient.get();
    }

    ExternalApplicationTokenRefresher tokenRefresher = WebServiceTokenRefreshers
        .externalApplication(logger, basePath, keys);
    externalApplicationClient = ThreadLocal.withInitial(() -> new ExternalApplicationClient(tokenRefresher));
    return externalApplicationClient.get();
  }

  /**
   * Represents a player playing on a Minecraft server. Commands issued by non-OP players
   * should always use this client type.
   */
  public PlayerClient player(UUID playerId) {
    if (playerClients.containsKey(playerId)) {
      return playerClients.get(playerId);
    }

    PlayerTokenRefresher tokenRefresher = WebServiceTokenRefreshers
        .player(logger, basePath, playerId, this);
    PlayerClient playerClient = new PlayerClient(tokenRefresher);
    playerClients.put(playerId, playerClient);
    return playerClient;
  }

  public void shutdown() {
    executorService.shutdownNow();
  }

  public interface Logger {
    void logWarning(String message);

    void logInfo(String message);
  }
}
