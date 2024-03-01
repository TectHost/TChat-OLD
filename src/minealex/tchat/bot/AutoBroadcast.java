package minealex.tchat.bot;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import me.clip.placeholderapi.PlaceholderAPI;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AutoBroadcast {

    private final JavaPlugin plugin;
    private final FileConfiguration autoBroadcastConfig;
    private static final int MAX_LINE_LENGTH = 70;
    private static final int NEW_MAX_LINE_LENGTH = 60;

    public AutoBroadcast(JavaPlugin plugin) {
        this.plugin = plugin;
        this.autoBroadcastConfig = loadAutoBroadcastConfig();

        if (autoBroadcastConfig == null) {
            plugin.getLogger().severe("The autobroadcast configuration could not be loaded.");
        } else {
            startBroadcastTask();
        }
    }

    private FileConfiguration loadAutoBroadcastConfig() {
        File configFile = new File(plugin.getDataFolder(), "autobroadcast.yml");

        if (!configFile.exists()) {
            plugin.saveResource("autobroadcast.yml", false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        if (config == null) {
            plugin.getLogger().severe("Failed to load autobroadcast.yml");
            return null;
        }

        return config;
    }

    private void startBroadcastTask() {
        boolean broadcastsEnabled = isBroadcastsEnabled();

        if (!broadcastsEnabled) {
            return;
        }

        int tiempoEntreBroadcasts = getTime();

        new BukkitRunnable() {
            int index = 0;
            List<String> broadcastKeys = new ArrayList<>(getBroadcasts().getKeys(false));

            @Override
            public void run() {
                if (index >= broadcastKeys.size()) {
                    index = 0;
                }

                String broadcastKey = broadcastKeys.get(index);
                ConfigurationSection broadcast = getBroadcasts().getConfigurationSection(broadcastKey);
                List<String> messageList = broadcast.getStringList("messages");

                for (String message : messageList) {
                    message = ChatColor.translateAlternateColorCodes('&', message);
                    
                    if (message.contains("%center%")) {
                        int messageLength = getVisibleLength(message.replace("%center%", ""));
                        int padding = (MAX_LINE_LENGTH - messageLength) / 2;
                        message = message.replace("%center%", repeat(" ", padding));
                    }
                    
                    if (message.contains("%newer_center%")) {
                        int messageLength = getVisibleLength(message.replace("%newer_center%", ""));
                        int padding = (NEW_MAX_LINE_LENGTH - messageLength) / 2;
                        message = message.replace("%newer_center%", repeat(" ", padding));
                    }

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        String formattedMessage = PlaceholderAPI.setPlaceholders(player, message);
                        player.sendMessage(formattedMessage);

                        if (isTitleEnabled()) {
                            boolean titleEnabled = broadcast.getBoolean("title-enabled", true);
                            if (titleEnabled) {
                                String title = broadcast.getString("title");
                                String subTitle = broadcast.getString("sub-title");

                                if (title != null && subTitle != null) {
                                    sendTitleSubtitle(player, title, subTitle);
                                }
                            }
                        }

                        if (broadcast.contains("sound") && broadcast.getBoolean("sound-enabled", true)) {
                            String soundName = broadcast.getString("sound");
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

    public ConfigurationSection getBroadcasts() {
        ConfigurationSection broadcastsSection = autoBroadcastConfig.getConfigurationSection("broadcasts");

        if (broadcastsSection == null) {
            plugin.getLogger().severe("The 'broadcasts' section is missing or empty in autobroadcast.yml");
            return new MemoryConfiguration();
        }

        return broadcastsSection;
    }

    public boolean isBroadcastsEnabled() {
        return autoBroadcastConfig.getBoolean("enabled", true);
    }
    
    private int getVisibleLength(String input) {
        return ChatColor.stripColor(input).length();
    }

    private String repeat(String str, int times) {
        if (times < 0) {
            times = 0;  // Set times to 0 if it's negative
        }
        return new String(new char[times]).replace("\0", str);
    }

    public boolean isTitleEnabled() {
        return autoBroadcastConfig.getBoolean("titleEnabled", true);
    }

    public int getTime() {
        return autoBroadcastConfig.getInt("time", 45);
    }

    @SuppressWarnings("deprecation")
    public void sendTitleSubtitle(Player player, String title, String subTitle) {
        player.sendTitle(ChatColor.translateAlternateColorCodes('&', title), ChatColor.translateAlternateColorCodes('&', subTitle));
    }
}
