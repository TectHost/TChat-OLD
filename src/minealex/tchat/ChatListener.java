package minealex.tchat;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unused")
public class ChatListener implements Listener {
    private TChat plugin;

    public ChatListener(TChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        // Check if the message contains any banned words
        if (plugin.getBannedWords().isWordBanned(message.toLowerCase()) && !plugin.getBannedWords().canBypassBannedWords(player)) {
            plugin.getBannedWords().sendBlockedMessage(player);
            event.setCancelled(true);
            return;
        }
        UUID playerId = player.getUniqueId();
			if (!plugin.hasPlayerMoved(playerId)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Debes moverte antes de poder chatear.");
                return;
            }
        // Format the message and set the chat format
        String format = plugin.formatMessage(event.getMessage(), event.getPlayer());
        event.setFormat(format);
    }

    // Agrega un método para verificar si el jugador se ha movido
    private boolean hasPlayerMoved(Player player) {
        Location lastLocation = plugin.getLastPlayerLocation(player);

        // Verificar si la ubicación actual es la misma que la última conocida
        if (lastLocation == null || !lastLocation.equals(player.getLocation())) {
            // Actualizar la última ubicación conocida
            plugin.setLastPlayerLocation(player, player.getLocation());
            return true;
        }

        return false;
    }
}
