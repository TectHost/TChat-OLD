package minealex.tchat.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;

public class MeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	if (!sender.hasPermission("tchat.me")) {
            sender.sendMessage(getMessage("noPermission"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Correct use: /me <message>");
            return true;
        }
        
        String playerName = sender.getName();

        StringBuilder message = new StringBuilder();
        for (String word : args) {
            message.append(word).append(" ");
        }

        try {
            JSONParser parser = new JSONParser();
            JSONObject config = (JSONObject) parser.parse(new FileReader("plugins/TChat/format_config.json"));
            JSONObject broadcastConfig = (JSONObject) config.get("Announcements");
            String format = (String) broadcastConfig.get("meFormat");

            String formattedMessage = ChatColor.translateAlternateColorCodes('&', format.replace("%s", message.toString().trim()).replace("%player%", playerName));
            Bukkit.broadcastMessage(formattedMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }
    
    private String getMessage(String key) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject config = (JSONObject) parser.parse(new FileReader("plugins/TChat/format_config.json"));
            JSONObject broadcastConfig = (JSONObject) config.get("messages");
            return ChatColor.translateAlternateColorCodes('&', (String) broadcastConfig.get(key));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
