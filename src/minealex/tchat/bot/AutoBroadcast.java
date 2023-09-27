package minealex.tchat.bot;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import me.clip.placeholderapi.PlaceholderAPI;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class AutoBroadcast {

    private final JavaPlugin plugin;
    private final Map<String, Object> autoBroadcastData;

    public AutoBroadcast(JavaPlugin plugin) {
        this.plugin = plugin;
        this.autoBroadcastData = loadAutoBroadcast();

        if (autoBroadcastData.isEmpty()) {
            plugin.getLogger().severe("No se pudo cargar la configuración del autobroadcast.");
        } else {
            startBroadcastTask();
        }
    }

    private Map<String, Object> loadAutoBroadcast() {
        File configFile = new File(plugin.getDataFolder(), "autobroadcast.json");

        if (!configFile.exists()) {
            plugin.saveResource("autobroadcast.json", false);
        }

        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(configFile));
            Map<String, Object> autoBroadcastData = new HashMap<>();

            int time = ((Long) jsonObject.get("time")).intValue();
            autoBroadcastData.put("time", time);

            @SuppressWarnings("unchecked")
			boolean enabled = (boolean) jsonObject.getOrDefault("enabled", true); // Asegúrate de añadir esta línea
            autoBroadcastData.put("enabled", enabled); // Asegúrate de añadir esta línea

            JSONObject broadcasts = (JSONObject) jsonObject.get("broadcasts");
            Map<String, List<String>> broadcastMap = new HashMap<>();

            for (Object key : broadcasts.keySet()) {
                String broadcastKey = (String) key;
                JSONArray messagesArray = (JSONArray) broadcasts.get(broadcastKey);
                List<String> messagesList = new ArrayList<>();

                for (Object message : messagesArray) {
                    messagesList.add((String) message);
                }

                broadcastMap.put(broadcastKey, messagesList);
            }

            autoBroadcastData.put("broadcasts", broadcastMap);

            return autoBroadcastData;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error loading autobroadcast.json, no data will be available.", e);
            return new HashMap<>();
        }
    }

    private void startBroadcastTask() {
        boolean broadcastsEnabled = isBroadcastsEnabled(); // Método para obtener el estado de los broadcasts

        if (!broadcastsEnabled) {
            return; // Si los broadcasts están deshabilitados, no hacemos nada
        }
        
        int tiempoEntreBroadcasts = getTime();

        new BukkitRunnable() {
            int index = 0;
            List<String> broadcastKeys = new ArrayList<>(getBroadcasts().keySet());

            @Override
            public void run() {
                if (index >= broadcastKeys.size()) {
                    index = 0;
                }

                String broadcastKey = broadcastKeys.get(index);
                List<String> messageList = getBroadcasts().get(broadcastKey);

                for (String message : messageList) {
                    // Aplicar códigos de formato de Minecraft y placeholders
                    message = ChatColor.translateAlternateColorCodes('&', message);

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        String formattedMessage = PlaceholderAPI.setPlaceholders(player, message);

                        // Envía el mensaje a los jugadores o realiza cualquier acción que desees
                        player.sendMessage(formattedMessage);
                    }
                }

                index++;
            }
        }.runTaskTimer(plugin, 0, tiempoEntreBroadcasts * 20); // Convierte los segundos a ticks
    }
    
    public Map<String, List<String>> getBroadcasts() {
        @SuppressWarnings("unchecked")
        Map<String, List<String>> broadcasts = (Map<String, List<String>>) autoBroadcastData.getOrDefault("broadcasts", new HashMap<>());
        return broadcasts;
    }
    
    public boolean isBroadcastsEnabled() {
        return (boolean) autoBroadcastData.getOrDefault("enabled", true);
    }

    public int getTime() {
        return (int) autoBroadcastData.getOrDefault("time", 45);
    }

    public List<String> getBroadcast(String key) {
        @SuppressWarnings("unchecked")
        Map<String, List<String>> broadcasts = (Map<String, List<String>>) autoBroadcastData.getOrDefault("broadcasts", new HashMap<>());
        return broadcasts.getOrDefault(key, new ArrayList<>());
    }
}
