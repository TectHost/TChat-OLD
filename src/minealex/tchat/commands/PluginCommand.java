package minealex.tchat.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

public class PluginCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public PluginCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sender.sendMessage("Usage: /plugin <plugin_name>");
            return true;
        }

        String targetPluginName = args[0];

        try {
            File configFile = new File(plugin.getDataFolder(), "format_config.json");

            if (configFile.exists()) {
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(configFile));

                JSONArray rulesArray = (JSONArray) jsonObject.get("Plugin");

                Plugin targetPlugin = plugin.getServer().getPluginManager().getPlugin(targetPluginName);

                if (targetPlugin != null) {
                    String pluginName = targetPlugin.getName();
                    String pluginVersion = getPluginVersion(targetPlugin);
                    int ramUsageMB = getRamUsage(targetPlugin);

                    // Reemplazar placeholders en cada regla y enviar el mensaje
                    for (Object rule : rulesArray) {
                        String message = (String) rule;
                        message = message.replace("%plugin%", ChatColor.translateAlternateColorCodes('&', pluginName));
                        message = message.replace("%version%", ChatColor.translateAlternateColorCodes('&', pluginVersion));
                        message = message.replace("%ram%", String.valueOf(ramUsageMB));
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                    }
                } else {
                    sender.sendMessage("Plugin '" + targetPluginName + "' not found.");
                }

            } else {
                sender.sendMessage("File 'format_config.json' was not found.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    private String getPluginVersion(Plugin targetPlugin) {
        return targetPlugin.getDescription().getVersion();
    }

    private int getRamUsage(Plugin targetPlugin) {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();

        // Obtener la cantidad de memoria utilizada en megabytes
        long usedMemoryMB = heapMemoryUsage.getUsed() / (1024 * 1024);

        return (int) usedMemoryMB;
    }
}
