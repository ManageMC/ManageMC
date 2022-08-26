package com.managemc.spigot.service;

import com.managemc.api.ApiException;
import com.managemc.spigot.config.SpigotPluginConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;
import org.openapitools.client.model.CreateNoteInput;
import org.openapitools.client.model.PlayerNote;

public class NoteService {

  private static final String KICK_NOTE = "Kicked from in-game server %s #%s. Reason: %s.";

  private final SpigotPluginConfig config;

  public NoteService(SpigotPluginConfig config) {
    this.config = config;
  }

  public PlayerNote createNoteForKickedPlayer(PlayerKickEvent event) throws ApiException {
    String note = String.format(
        KICK_NOTE,
        config.getLocalConfig().getServerGroup(),
        config.getLocalConfig().getDynamicComponents().getServerMetadata().getServerId(),
        event.getReason()
    );
    CreateNoteInput input = new CreateNoteInput()
        .note(note)
        .isHidden(false);

    try {
      return createNote(event.getPlayer(), input);
    } catch (ApiException e) {
      // remember that if we implement a /note command, we'll need to check for 403 here as well
      if (e.getCode() != 404) {
        throw e;
      }
      return null;
    }
  }

  public PlayerNote createNote(Player player, CreateNoteInput noteInput) throws ApiException {
    return config.getClientProvider().externalServer().getNotesApi()
        .createNote(player.getUniqueId().toString(), noteInput);
  }
}
