package minealex.tchat;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

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

        // Format the message and set the chat format
        String format = plugin.formatMessage(event.getMessage(), event.getPlayer());
        event.setFormat(format);
    }
}
