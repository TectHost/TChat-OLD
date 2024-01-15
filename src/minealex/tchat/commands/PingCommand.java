package minealex.tchat.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import net.minecraft.server.v1_8_R3.EntityPlayer;

public class PingCommand implements CommandExecutor {

    private Plugin plugin;

    public PingCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String noPermissionMessage = getNoPermissionMessage();

        if (sender instanceof Player) {
            Player player = (Player) sender;

            // Verificar el permiso
            if (!player.hasPermission("tchat.ping")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', noPermissionMessage));
                return true;
            }

            // Obtener el ping
            int ping = getPing(player);

            // Obtener el mensaje personalizado del archivo format_config.json
            String pingMessage = getCustomMessage();
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', pingMessage.replace("%ping%", String.valueOf(ping))));
        } else {
            sender.sendMessage(noPermissionMessage);
        }

        return true;
    }

    public int getPing(Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        EntityPlayer entityPlayer = craftPlayer.getHandle();
        return entityPlayer.ping;
    }
    
    private String getNoPermissionMessage() {
        // Obtener el archivo format_config.json
        File configFile = new File(plugin.getDataFolder(), "format_config.json");

        try (FileReader reader = new FileReader(configFile)) {
            // Leer el archivo JSON
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(reader);

            // Obtener el mensaje personalizado de falta de permisos de la sección messages
            JSONObject messages = (JSONObject) json.get("messages");
            String noPermissionMessage = (String) messages.get("noPermission");
            
            return noPermissionMessage;
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            return "Error al obtener el mensaje de falta de permisos.";
        }
    }
    
    private String getCustomMessage() {
        // Obtener el archivo format_config.json
        File configFile = new File(plugin.getDataFolder(), "format_config.json");

        try (FileReader reader = new FileReader(configFile)) {
            // Leer el archivo JSON
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(reader);

            // Obtener la sección de mensajes
            JSONObject messages = (JSONObject) json.get("messages");

            // Obtener el mensaje personalizado del ping
            String pingMessage = (String) messages.get("ping_message");

            return pingMessage;
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            return "Error al obtener el mensaje personalizado.";
        }
    }
}
