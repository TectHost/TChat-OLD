package minealex.tchat.commands;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.net.InetAddress;
import java.util.UUID;

public class PlayerCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public PlayerCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("tchat.player")) {
            sender.sendMessage(getMessage("noPermission"));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(getMessage("incorrectUsagePlayer"));
            return false;
        }

        String playerName = args[0];
        Player targetPlayer = Bukkit.getPlayerExact(playerName);

        if (targetPlayer == null) {
            sender.sendMessage(getMessage("noPlayerOnline"));
            return true;
        }

        try {
            File configFile = new File(plugin.getDataFolder(), "format_config.json");

            if (configFile.exists()) {
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(configFile));

                JSONArray rulesArray = (JSONArray) jsonObject.get("player_message");

                // Enviar cada regla con placeholders
                for (Object rule : rulesArray) {
                    String formattedRule = formatPlaceholder((String) rule, targetPlayer);
                    String coloredRule = ChatColor.translateAlternateColorCodes('&', formattedRule);
                    sender.sendMessage(coloredRule);
                }
            } else {
                sender.sendMessage("File 'format_config.json' was not found.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    private String formatPlaceholder(String rule, Player targetPlayer) {
    	InetAddress address = targetPlayer.getAddress().getAddress();
    	String ipAddress = address.getHostAddress();
    	UUID playerUUID = targetPlayer.getUniqueId();
    	
    	rule = rule.replace("%ip%", ipAddress);
    	rule = rule.replace("%uuid%", playerUUID.toString());
    	rule = rule.replace("%player%", targetPlayer.getName());
        rule = PlaceholderAPI.setPlaceholders(targetPlayer, rule);

        return rule;
    }

    private String getMessage(String key) {
        File configFile = new File(plugin.getDataFolder(), "format_config.json");

        try {
            if (configFile.exists()) {
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(configFile));
                JSONObject messagesObject = (JSONObject) jsonObject.get("messages");

                return ChatColor.translateAlternateColorCodes('&', (String) messagesObject.get(key));
            } else {
                return "File 'format_config.json' was not found.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "An error occurred while fetching the message.";
        }
    }
}
