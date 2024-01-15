package minealex.tchat.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class IgnoreCommand implements CommandExecutor {
    private File savesFile;
    private FileConfiguration savesConfig;
    private File messagesFile;
    private FileConfiguration messagesConfig;

    public IgnoreCommand(Plugin plugin) {
        savesFile = new File(plugin.getDataFolder(), "saves.yml");
        savesConfig = YamlConfiguration.loadConfiguration(savesFile);
        
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(getConfiguredFormat("messages.playersOnly"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("tchat.ignore")) {
            player.sendMessage(getConfiguredFormat("messages.noPermission"));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(getConfiguredFormat("messages.incorrectIgnoreUsage"));
            return true;
        }

        String ignoredPlayerName = args[0];
        UUID playerUUID = player.getUniqueId();

        // Access saves.yml configuration
        List<String> ignoredPlayers = savesConfig.getStringList("players." + playerUUID + ".ignore");

        if (ignoredPlayers.contains(ignoredPlayerName)) {
            ignoredPlayers.remove(ignoredPlayerName);
            player.sendMessage(getConfiguredFormat("messages.ignoreRemove").replace("%ignored%", ignoredPlayerName));
        } else {
            ignoredPlayers.add(ignoredPlayerName);
            player.sendMessage(getConfiguredFormat("messages.ignoreAdd").replace("%ignored%", ignoredPlayerName));
        }

        savesConfig.set("players." + playerUUID + ".ignore", ignoredPlayers);

        // Save the changes to the saves.yml file
        saveSavesConfig();

        return true;
    }

    private String getConfiguredFormat(String formatKey) {
        if (messagesConfig.contains(formatKey)) {
            return ChatColor.translateAlternateColorCodes('&', messagesConfig.getString(formatKey));
        } else {
            return "<sender> whispers to <recipient>: <message>";
        }
    }

    private void saveSavesConfig() {
        try {
            savesConfig.save(savesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
