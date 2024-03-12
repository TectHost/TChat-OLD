package minealex.tchat.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import me.clip.placeholderapi.PlaceholderAPI;
import minealex.tchat.TChat;

import java.io.File;
import java.util.List;

public class SeenCommand implements CommandExecutor {

    private final TChat plugin;
    private final YamlConfiguration messagesConfig;

    public SeenCommand(TChat plugin) {
        this.plugin = plugin;

        // Cargar configuración de mensajes desde el archivo
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        this.messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

        // Verificar el permiso tchat.seen o tchat.seen.full
        if (player.hasPermission("tchat.seen.full")) {
            sendMessage(player, "Seen.full-seen-message", player.getName());
        } else if (player.hasPermission("tchat.seen")) {
            sendMessage(player, "Seen.seen-message", player.getName());
        } else {
           plugin.getMessagesYML("messages.noPermission");
        }

        return true;
    }

    private void sendMessage(Player player, String key, String playerName) {
        // Verificar si la clave existe en la configuración
        if (messagesConfig.isSet(key)) {
            List<String> messages = messagesConfig.getStringList(key);

            // Aplicar colores y enviar los mensajes al jugador con PlaceholderAPI
            messages.forEach(message -> {
                message = message.replace("%player%", playerName);
                message = ChatColor.translateAlternateColorCodes('&', message);
                player.sendMessage(PlaceholderAPI.setPlaceholders(player, message));
            });
        } else {
            // Mensaje de error si la clave no se encuentra en el archivo messages.yml
            player.sendMessage("Error: La clave '" + key + "' no se encuentra en el archivo messages.yml.");
        }
    }
}
