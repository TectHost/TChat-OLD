package minealex.tchat.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;

public class WarningCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	if (!sender.hasPermission("tchat.warning")) {
            sender.sendMessage(getMessage("noPermission"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Correct use: /warning <message>");
            return true;
        }

        StringBuilder message = new StringBuilder();
        for (String word : args) {
            message.append(word).append(" ");
        }

        try {
            JSONParser parser = new JSONParser();
            JSONObject config = (JSONObject) parser.parse(new FileReader("plugins/TChat/format_config.json"));
            JSONObject broadcastConfig = (JSONObject) config.get("Announcements");
            String format = (String) broadcastConfig.get("warningFormat");

            String formattedMessage = ChatColor.translateAlternateColorCodes('&', format.replace("%s", message.toString().trim()));
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
