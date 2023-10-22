package minealex.tchat.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.List;
import java.util.UUID;

public class IgnoreCommand implements CommandExecutor {
    private final Plugin plugin;

    public IgnoreCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + getConfiguredFormat("playersOnly"));
            return true;
        }

        Player player = (Player) sender;
        
        if (!player.hasPermission("tchat.ignore")) {
            player.sendMessage(ChatColor.RED + getConfiguredFormat("noPermission"));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + getConfiguredFormat("incorrectIgnoreUsage"));
            return true;
        }

        String ignoredPlayerName = args[0];
        UUID playerUUID = player.getUniqueId();
        List<String> ignoredPlayers = plugin.getConfig().getStringList("players." + playerUUID + ".ignore");

        if (ignoredPlayers.contains(ignoredPlayerName)) {
            ignoredPlayers.remove(ignoredPlayerName);
            player.sendMessage(ChatColor.GREEN + getConfiguredFormat("ignoreRemove").replace("%ignored%", ignoredPlayerName));
        } else {
            ignoredPlayers.add(ignoredPlayerName);
            player.sendMessage(ChatColor.GREEN + getConfiguredFormat("ignoreAdd").replace("%ignored%", ignoredPlayerName));
        }

        plugin.getConfig().set("players." + playerUUID + ".ignore", ignoredPlayers);
        plugin.saveConfig();

        return true;
    }

    private String getConfiguredFormat(String formatKey) {
        try {
            String filePath = plugin.getDataFolder().getPath() + "/format_config.json";
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader(filePath));
            JSONObject jsonObject = (JSONObject) obj;

            JSONObject msgFormats = (JSONObject) jsonObject.get("msgFormats");
            JSONObject messages = (JSONObject) jsonObject.get("messages");

            if (msgFormats.containsKey(formatKey)) {
                return ChatColor.translateAlternateColorCodes('&', (String) msgFormats.get(formatKey));
            } else if (messages.containsKey(formatKey)) {
                return ChatColor.translateAlternateColorCodes('&', (String) messages.get(formatKey));
            } else {
                return "<sender> whispers to <recipient>: <message>";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "<sender> whispers to <recipient>: <message>";
        }
    }
}
