package minealex.tchat.listener;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import me.clip.placeholderapi.PlaceholderAPI;

import minealex.tchat.TChat;

public class JoinListener implements Listener {

    private final TChat plugin;

    public JoinListener(TChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        try {
            boolean motdEnabled = isMotdEnabled();
            if (motdEnabled) {
                // Enviar el mensaje de MOTD al jugador
                sendMotdMessage(event.getPlayer().getName());
            }

            // Obtener y configurar el mensaje de join
            String joinMessage = getConfiguredJoinMessage(event.getPlayer().getName());
            if (joinMessage != null) {
                event.setJoinMessage(joinMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        try {
            // Get and set the quit message
            String quitMessage = getConfiguredQuitMessage(event.getPlayer().getName());
            if (quitMessage != null) {
                event.setQuitMessage(quitMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isMotdEnabled() {
        FileConfiguration config = plugin.getConfig();
        return config.getBoolean("Motd.motdEnabled", true);
    }

    @SuppressWarnings("deprecation")
	private String getConfiguredJoinMessage(String playerName) {
        FileConfiguration config = plugin.getConfig();
        boolean joinMessagesEnabled = config.getBoolean("Joins.joinMessagesEnabled", true);

        if (!joinMessagesEnabled) {
            return null;
        }

        String joinMessage = config.getString("Joins.JoinMessage");
        if (joinMessage != null) {
            joinMessage = joinMessage.replace("%player%", playerName);
            joinMessage = PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(playerName), joinMessage);
            return ChatColor.translateAlternateColorCodes('&', joinMessage);
        }

        return null;
    }

    @SuppressWarnings("deprecation")
	private String getConfiguredQuitMessage(String playerName) {
        FileConfiguration config = plugin.getConfig();
        boolean quitMessagesEnabled = config.getBoolean("Joins.quitMessagesEnabled", true);

        if (!quitMessagesEnabled) {
            return null;
        }

        String quitMessage = config.getString("Joins.QuitMessage");
        if (quitMessage != null) {
            quitMessage = quitMessage.replace("%player%", playerName);
            quitMessage = PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(playerName), quitMessage);
            return ChatColor.translateAlternateColorCodes('&', quitMessage);
        }

        return null;
    }

    private void sendMotdMessage(String playerName) {
        FileConfiguration config = plugin.getConfig();
        if (config.contains("Motd.motdEnabled") && config.getBoolean("Motd.motdEnabled")) {
            if (config.contains("Motd.motdMessage")) {
                for (String message : config.getStringList("Motd.motdMessage")) {
                    // Send each line of the MOTD message to the player
                    String formattedMessage = ChatColor.translateAlternateColorCodes('&', message);
                    Bukkit.getPlayer(playerName).sendMessage(formattedMessage);
                }
            }
        }
    }
}
