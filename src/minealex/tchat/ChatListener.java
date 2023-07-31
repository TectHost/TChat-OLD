// ChatListener.java
package minealex.tchat;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

@SuppressWarnings("unused")
public class ChatListener implements Listener {
    private final TChat plugin;

    public ChatListener(TChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String format = plugin.formatMessage(event.getMessage(), event.getPlayer());
        event.setFormat(format);
    }
}
