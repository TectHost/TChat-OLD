package minealex.tchat.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import me.clip.placeholderapi.PlaceholderAPI;

import java.io.FileReader;

import minealex.tchat.TChat;

public class JoinListener implements Listener {

	private final TChat plugin;

    public JoinListener(TChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        try {
            String joinMessage = getConfiguredJoinMessage(event.getPlayer().getName()); // Obtener el JoinMessage desde el archivo JSON
            event.setJoinMessage(joinMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        try {
            String quitMessage = getConfiguredQuitMessage(event.getPlayer().getName()); // Obtener el QuitMessage desde el archivo JSON
            event.setQuitMessage(quitMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
	private String getConfiguredJoinMessage(String playerName) {
        try {
            String filePath = plugin.getDataFolder().getPath() + "/format_config.json";
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader(filePath));
            JSONObject jsonObject = (JSONObject) obj;

            JSONObject joins = (JSONObject) jsonObject.get("Joins");

            if (joins != null && joins.containsKey("JoinMessage")) {
                String joinMessage = (String) joins.get("JoinMessage");
                joinMessage = joinMessage.replace("%player%", playerName); // Reemplazar %player% con el nombre del jugador
                joinMessage = PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(playerName), joinMessage);
                return ChatColor.translateAlternateColorCodes('&', joinMessage);
            } else {
                return "&5TChat &e> &7[&a+&7] &a" + playerName + " &7has entered the server";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "&5TChat &e> &7[&a+&7] &a" + playerName + " &7has entered the server";
        }
    }

    @SuppressWarnings("deprecation")
	private String getConfiguredQuitMessage(String playerName) {
        try {
            String filePath = plugin.getDataFolder().getPath() + "/format_config.json";
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader(filePath));
            JSONObject jsonObject = (JSONObject) obj;

            JSONObject joins = (JSONObject) jsonObject.get("Joins");

            if (joins != null && joins.containsKey("QuitMessage")) {
                String quitMessage = (String) joins.get("QuitMessage");
                quitMessage = quitMessage.replace("%player%", playerName); // Reemplazar %player% con el nombre del jugador
                quitMessage = PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(playerName), quitMessage);
                return ChatColor.translateAlternateColorCodes('&', quitMessage);
            } else {
                return "&5TChat &e> &7[&c-&7] &c" + playerName + " &7has left the server";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "&5TChat &e> &7[&c-&7] &c" + playerName + " &7has left the server";
        }
    }
}
