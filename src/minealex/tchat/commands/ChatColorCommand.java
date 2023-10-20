package minealex.tchat.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import me.clip.placeholderapi.PlaceholderAPI;

import java.io.FileReader;

public class ChatColorCommand implements CommandExecutor {
    private final Plugin plugin;

    public ChatColorCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(getMessages("onlyPlayer"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 2) {
            player.sendMessage(getMessages("chatColorIncorrectUsage"));
            return true;
        }

        String colorName = args[0].toLowerCase();
        ChatColor chatColor = getColorFromString(colorName);

        if (chatColor == null) {
            player.sendMessage(getMessages("chatColorInvalid"));
            return true;
        }

        String format = args[1].toLowerCase(); // Obtener el formato

        // Verificar si el jugador tiene el permiso adecuado para el formato
        if (!player.hasPermission("tchat.chatcolor." + colorName + "." + format)) {
            player.sendMessage(getMessages("noPermission"));
            return true;
        }

        // Aplicar el color y el formato al jugador
        String chatColorSuccess = getMessages("chatColorSuccess");
        chatColorSuccess = PlaceholderAPI.setPlaceholders(player, chatColorSuccess);
        chatColorSuccess = chatColorSuccess.replace("%color%", chatColor.toString());
        chatColorSuccess = chatColorSuccess.replace("%format%", format);

        chatColorSuccess = ChatColor.translateAlternateColorCodes('&', chatColorSuccess);
        player.sendMessage(chatColorSuccess);
        plugin.getConfig().set("players." + player.getUniqueId() + ".chatcolor", chatColor.name());
        plugin.getConfig().set("players." + player.getUniqueId() + ".format", format); // Guardar el formato
        plugin.saveConfig();

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

    private String getMessages(String formatKey) {
        try {
            String filePath = plugin.getDataFolder().getPath() + "/format_config.json";
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader(filePath));
            JSONObject jsonObject = (JSONObject) obj;

            JSONObject messages = (JSONObject) jsonObject.get("messages");

            if (messages.containsKey(formatKey)) {
                return ChatColor.translateAlternateColorCodes('&', (String) messages.get(formatKey));
            } else {
                return "Error in format_config.json file"; // Puedes establecer un mensaje predeterminado aquí.
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error in format_config.json file"; // Puedes establecer un mensaje predeterminado aquí.
        }
    }
}
