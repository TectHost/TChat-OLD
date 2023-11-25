package minealex.tchat.bot;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
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

    @SuppressWarnings("unchecked")
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

            boolean enabled = (boolean) jsonObject.getOrDefault("enabled", true);
            autoBroadcastData.put("enabled", enabled);

            JSONObject broadcasts = (JSONObject) jsonObject.get("broadcasts");
            Map<String, Map<String, Object>> broadcastMap = new HashMap<>();

            for (Object key : broadcasts.keySet()) {
                String broadcastKey = (String) key;
                JSONObject broadcastObj = (JSONObject) broadcasts.get(broadcastKey);

                List<String> messagesList = new ArrayList<>();
                JSONArray messagesArray = (JSONArray) broadcastObj.get("messages");
                for (Object message : messagesArray) {
                    messagesList.add((String) message);
                }

                Map<String, Object> broadcastData = new HashMap<>();
                broadcastData.put("messages", messagesList);
                broadcastData.put("title-enabled", broadcastObj.getOrDefault("title-enabled", true));
                broadcastData.put("title", broadcastObj.get("title"));
                broadcastData.put("sub-title", broadcastObj.get("sub-title"));

                broadcastMap.put(broadcastKey, broadcastData);
            }

            autoBroadcastData.put("broadcasts", broadcastMap);

            return autoBroadcastData;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error loading autobroadcast.json, no data will be available.", e);
            return new HashMap<>();
        }
    }

    private void startBroadcastTask() {
        boolean broadcastsEnabled = isBroadcastsEnabled();

        if (!broadcastsEnabled) {
            return;
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
                Map<String, Object> broadcast = getBroadcasts().get(broadcastKey);
                @SuppressWarnings("unchecked")
                List<String> messageList = (List<String>) broadcast.get("messages");

                for (String message : messageList) {
                    message = ChatColor.translateAlternateColorCodes('&', message);

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        String formattedMessage = PlaceholderAPI.setPlaceholders(player, message);
                        player.sendMessage(formattedMessage);

                        if (isTitleEnabled()) {
                            boolean titleEnabled = (boolean) broadcast.getOrDefault("title-enabled", true);
                            if (titleEnabled) {
                                String title = (String) broadcast.get("title");
                                String subTitle = (String) broadcast.get("sub-title");

                                if (title != null && subTitle != null) {
                                    sendTitleSubtitle(player, title, subTitle);
                                }
                            }
                        }

                        // Reproduce el sonido si está configurado
                        if (broadcast.containsKey("sound")) {
                            String soundName = (String) broadcast.get("sound");
                            try {
                                Sound sound = Sound.valueOf(soundName);
                                player.playSound(player.getLocation(), sound, 1.0F, 1.0F);
                            } catch (IllegalArgumentException e) {
                                plugin.getLogger().warning("Sound '" + soundName + "' not found!");
                            }
                        }
                    }
                }

                index++;
            }
        }.runTaskTimer(plugin, 0, tiempoEntreBroadcasts * 20);
    }

    public Map<String, Map<String, Object>> getBroadcasts() {
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> broadcasts = (Map<String, Map<String, Object>>) autoBroadcastData.getOrDefault("broadcasts", new HashMap<>());
        return broadcasts;
    }

    public boolean isBroadcastsEnabled() {
        return (boolean) autoBroadcastData.getOrDefault("enabled", true);
    }

    public boolean isTitleEnabled() {
        return (boolean) autoBroadcastData.getOrDefault("titleEnabled", true);
    }

    public int getTime() {
        return (int) autoBroadcastData.getOrDefault("time", 45);
    }

    @SuppressWarnings("deprecation")
	public void sendTitleSubtitle(Player player, String title, String subTitle) {
        player.sendTitle(ChatColor.translateAlternateColorCodes('&', title), ChatColor.translateAlternateColorCodes('&', subTitle));
    }
}
