package minealex.tchat.bot;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import minealex.tchat.TChat;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ChatBot {
    private Map<String, String> responses;
    private TChat plugin;

    public ChatBot(TChat plugin) {
        this.plugin = plugin;
        this.responses = loadResponses();
    }

    private Map<String, String> loadResponses() {
        File configFile = new File(plugin.getDataFolder(), "chatbot.yml");

        if (!configFile.exists()) {
            plugin.saveResource("chatbot.yml", false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        Map<String, String> responses = new HashMap<>();

        Set<String> keys = config.getKeys(false);
        for (String key : keys) {
            responses.put(key, config.getString(key));
        }

        return responses;
    }

    public void sendResponse(String question, CommandSender sender) {
        if (responses.containsKey(question)) {
            String originalResponse = responses.get(question);

            if (originalResponse != null) {
                final String response = ChatColor.translateAlternateColorCodes('&', originalResponse);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        sender.sendMessage(response);
                    }
                }.runTaskLater(plugin, 2L); // 20 ticks equivalen a 1 segundo
            }
        }
    }

    public void reloadResponses() {
        responses = loadResponses();
    }
}
