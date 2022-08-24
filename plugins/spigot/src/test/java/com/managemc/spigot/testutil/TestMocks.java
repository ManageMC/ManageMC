package com.managemc.spigot.testutil;

import com.managemc.api.wrapper.model.Keys;
import com.managemc.plugins.bukkit.BukkitWrapper;
import com.managemc.plugins.logging.BukkitLogging;
import com.managemc.spigot.config.model.LocalConfig;
import com.managemc.spigot.config.model.PermissionsManager;
import com.managemc.spigot.config.model.ServerMetadata;
import com.managemc.spigot.state.PlayerData;
import lombok.Getter;
import org.mockito.Mockito;

public class TestMocks {

  @Getter
  private final BukkitLogging logging = Mockito.mock(BukkitLogging.class);
  @Getter
  private final BukkitWrapper bukkitWrapper = Mockito.mock(BukkitWrapper.class);
  @Getter
  private final LocalConfig localConfig = assembleLocalConfig();
  @Getter
  private final PlayerData playerData = new PlayerData();
  @Getter
  private final PermissionsManager permissionsManager = Mockito.mock(PermissionsManager.class);


  private static LocalConfig assembleLocalConfig() {
    LocalConfig localConfig = Mockito.mock(LocalConfig.class);
    Mockito.when(localConfig.getKeys()).thenReturn(new Keys(TestConstants.PUBLIC_KEY, TestConstants.PRIVATE_KEY));
    Mockito.when(localConfig.getServerGroup()).thenReturn(TestConstants.KITPVP_LABEL);
    Mockito.when(localConfig.getServerName()).thenReturn(TestConstants.SERVER_NAME);
    Mockito.when(localConfig.getVersion()).thenReturn(TestConstants.MANAGEMC_VERSION);
    ServerMetadata serverMetadata = ServerMetadata.builder()
        .serverGroupId(TestConstants.KITPVP_ID)
        .serverId(TestConstants.SERVER_ID)
        .build();
    LocalConfig.DynamicComponents dynamicComponents = new LocalConfig.DynamicComponents(serverMetadata);
    Mockito.when(localConfig.getDynamicComponents()).thenReturn(dynamicComponents);

    Mockito
        .doAnswer((method) -> Mockito.when(localConfig.getVersion()).thenReturn(method.getArgument(0)))
        .when(localConfig).setVersion(Mockito.anyString());

    Mockito
        .doAnswer((method) -> Mockito.when(localConfig.getDynamicComponents()).thenReturn(method.getArgument(0)))
        .when(localConfig).setDynamicComponents(Mockito.any());

    return localConfig;
  }
}
