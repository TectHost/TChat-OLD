package minealex.tchat.bot;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import minealex.tchat.TChat;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ChatBot {
    private Map<String, String> responses;
    private TChat plugin;

    public ChatBot(TChat plugin) {
        this.plugin = plugin;
        this.responses = loadResponses();
    }

    private Map<String, String> loadResponses() {
        File configFile = new File(plugin.getDataFolder(), "chatbot.json");

        if (!configFile.exists()) {
            plugin.saveResource("chatbot.json", false);
        }

        try {
            JsonObject jsonObject = (JsonObject) new JsonParser().parse(new FileReader(configFile));
            Map<String, String> responses = new HashMap<>();

            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                responses.put(entry.getKey(), entry.getValue().getAsString());
            }

            return responses;
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Error loading chatbot.json, no responses will be available.", e);
            return new HashMap<>();
        }
    }

    public void sendResponse(String question, CommandSender sender) {
        if (responses.containsKey(question)) {
            String response = responses.get(question);

            if (response != null) {
                response = ChatColor.translateAlternateColorCodes('&', response);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                    	String response = responses.get(question);
                    	response = ChatColor.translateAlternateColorCodes('&', response);
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
