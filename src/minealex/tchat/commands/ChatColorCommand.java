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
import java.io.IOException;

public class ChatColorCommand implements CommandExecutor {
    private final TChat plugin;

    public ChatColorCommand(TChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessagesYML("onlyPlayer"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 2) {
            player.sendMessage(plugin.getMessagesYML("chatColorIncorrectUsage"));
            return true;
        }

        String colorName = args[0].toLowerCase();
        ChatColor chatColor = getColorFromString(colorName);

        if (chatColor == null) {
            player.sendMessage(plugin.getMessagesYML("chatColorInvalid"));
            return true;
        }

        String format = args[1].toLowerCase(); // Obtener el formato

        // Verificar si el jugador tiene el permiso adecuado para el formato
        if (!player.hasPermission("tchat.chatcolor." + colorName + "." + format)) {
            player.sendMessage(plugin.getMessagesYML("messages.noPermission"));
            return true;
        }

        // Aplicar el color y el formato al jugador
        String chatColorSuccess = plugin.getMessagesYML("messages.chatColorSuccess");
        chatColorSuccess = PlaceholderAPI.setPlaceholders(player, chatColorSuccess);
        chatColorSuccess = chatColorSuccess.replace("%color%", chatColor.toString());
        chatColorSuccess = chatColorSuccess.replace("%format%", format);

        chatColorSuccess = ChatColor.translateAlternateColorCodes('&', chatColorSuccess);
        player.sendMessage(chatColorSuccess);

        // Guardar en el archivo saves.yml
        File savesFile = new File(plugin.getDataFolder(), "saves.yml");
        YamlConfiguration savesConfig = YamlConfiguration.loadConfiguration(savesFile);

        String uuid = player.getUniqueId().toString();

        savesConfig.set("players." + uuid + ".chatcolor", chatColor.name());
        savesConfig.set("players." + uuid + ".format", format); // Guardar el formato

        try {
            // Guardar los cambios en el archivo saves.yml
            savesConfig.save(savesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    private ChatColor getColorFromString(String colorName) {
        switch (colorName.toLowerCase()) {
            case "black":
                return ChatColor.BLACK;
            case "darkblue":
                return ChatColor.DARK_BLUE;
            case "darkgreen":
                return ChatColor.DARK_GREEN;
            case "darkaqua":
                return ChatColor.DARK_AQUA;
            case "darkred":
                return ChatColor.DARK_RED;
            case "darkpurple":
                return ChatColor.DARK_PURPLE;
            case "gold":
                return ChatColor.GOLD;
            case "gray":
                return ChatColor.GRAY;
            case "darkgray":
                return ChatColor.DARK_GRAY;
            case "blue":
                return ChatColor.BLUE;
            case "green":
                return ChatColor.GREEN;
            case "aqua":
                return ChatColor.AQUA;
            case "red":
                return ChatColor.RED;
            case "lightpurple":
                return ChatColor.LIGHT_PURPLE;
            case "yellow":
                return ChatColor.YELLOW;
            case "white":
                return ChatColor.WHITE;
            default:
                return null;
        }
    }
    
    @SuppressWarnings("unused")
	private ChatColor getFormatFromString(String formatName) {
        switch (formatName.toLowerCase()) {
            case "bold":
                return ChatColor.BOLD;
            case "italic":
                return ChatColor.ITALIC;
            case "underline":
                return ChatColor.UNDERLINE;
            case "strikethrough":
                return ChatColor.STRIKETHROUGH;
            case "magic":
                return ChatColor.MAGIC;
            default:
                return null;
        }
    }
}
