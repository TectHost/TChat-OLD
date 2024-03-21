package minealex.tchat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import minealex.tchat.TChat;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class IgnoreCommand implements CommandExecutor {
	private TChat plugin;
    private File savesFile;
    private FileConfiguration savesConfig;

    public IgnoreCommand(TChat plugin) {
    	this.plugin = plugin;
        savesFile = new File(plugin.getDataFolder(), "saves.yml");
        savesConfig = YamlConfiguration.loadConfiguration(savesFile);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessagesYML("messages.playersOnly"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("tchat.ignore")) {
            player.sendMessage(plugin.getMessagesYML("messages.noPermission"));
            return true;
        }
        
        if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
        	if (!player.hasPermission("tchat.ignore.list")) {
                player.sendMessage(plugin.getMessagesYML("messages.noPermissionList"));
                return true;
            }
            UUID playerUUID = player.getUniqueId();
            List<String> ignoredPlayers = savesConfig.getStringList("players." + playerUUID + ".ignore");

            if (ignoredPlayers.isEmpty()) {
                player.sendMessage(plugin.getMessagesYML("messages.noPlayersIgnored"));
            } else {
                StringBuilder ignoredList = new StringBuilder(plugin.getMessagesYML("messages.ignoreListHeader"));
                ignoredList.append("\n");
                boolean firstPlayer = true; // Flag para identificar el primer jugador ignorado
                for (String ignoredPlayer : ignoredPlayers) {
                    if (firstPlayer) {
                        ignoredList.append(ignoredPlayer); // No añadir ", " antes del primer jugador ignorado
                        firstPlayer = false; // Cambiar el estado del flag después de agregar el primer jugador ignorado
                    } else {
                        ignoredList.append(", ").append(ignoredPlayer); // Añadir ", " antes de los siguientes jugadores ignorados
                    }
                }
                player.sendMessage(ignoredList.toString());
            }
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(plugin.getMessagesYML("messages.incorrectIgnoreUsage"));
            return true;
        }

        String ignoredPlayerName = args[0];
        UUID playerUUID = player.getUniqueId();

        // Access saves.yml configuration
        List<String> ignoredPlayers = savesConfig.getStringList("players." + playerUUID + ".ignore");

        if (ignoredPlayers.contains(ignoredPlayerName)) {
            ignoredPlayers.remove(ignoredPlayerName);
            player.sendMessage(plugin.getMessagesYML("messages.ignoreRemove").replace("%ignored%", ignoredPlayerName));
        } else {
            ignoredPlayers.add(ignoredPlayerName);
            player.sendMessage(plugin.getMessagesYML("messages.ignoreAdd").replace("%ignored%", ignoredPlayerName));
        }

        savesConfig.set("players." + playerUUID + ".ignore", ignoredPlayers);

        // Save the changes to the saves.yml file
        saveSavesConfig();

        return true;
    }

    private void saveSavesConfig() {
        try {
            savesConfig.save(savesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
