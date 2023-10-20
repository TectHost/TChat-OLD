package minealex.tchat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;

public class NickCommand implements CommandExecutor {
    private final Plugin plugin;

    public NickCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(getMessages("onlyPlayer"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("tchat.nickname")) {
            player.sendMessage(getMessages("noPermission"));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(getMessages("nickCommandUsage"));
            return true;
        }

        // Obtener el nombre del argumento
        String nuevoNick = args[0];

        // Crear el archivo config.yml si no existe
        if (!plugin.getConfig().contains("players")) {
            plugin.getConfig().createSection("players");
            plugin.saveConfig();
        }

        // Actualizar el nick en el config.yml
        plugin.getConfig().set("players." + player.getUniqueId() + ".nick", nuevoNick);

        // Guardar la configuración
        plugin.saveConfig();

        // Aplicar el nuevo nick al jugador
        player.setDisplayName(nuevoNick);
        player.setPlayerListName(nuevoNick);

        player.sendMessage("Tu nuevo nick es: " + nuevoNick);

        return true;
    }

    private String getMessages(String formatKey) {
        try {
            String filePath = plugin.getDataFolder().getPath() + "/format_config.json";
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader(filePath));
            JSONObject jsonObject = (JSONObject) obj;

            JSONObject messages = (JSONObject) jsonObject.get("messages");

            if (messages.containsKey(formatKey)) {
                return (String) messages.get(formatKey);
            } else {
                return "Error en el archivo format_config.json";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error en el archivo format_config.json";
        }
    }
}
