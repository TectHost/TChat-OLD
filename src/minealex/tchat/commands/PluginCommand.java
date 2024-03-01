package minealex.tchat.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;
import java.util.Map;

public class PluginCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private File messagesFile;
    private FileConfiguration messagesConfig;

    public PluginCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    @SuppressWarnings("unchecked")
	@Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sender.sendMessage(getMessages("messages.usagePlugin"));
            return true;
        }

        String targetPluginName = args[0];

        try {
            File configFile = new File(plugin.getDataFolder(), "messages.yml");

            if (configFile.exists()) {
                Yaml yaml = new Yaml();
                Map<String, Object> config = (Map<String, Object>) yaml.load(new FileReader(configFile));

                List<String> rulesArray = (List<String>) config.get("Plugin");

                Plugin targetPlugin = plugin.getServer().getPluginManager().getPlugin(targetPluginName);

                if (targetPlugin != null) {
                    String pluginName = targetPlugin.getName();
                    String pluginVersion = getPluginVersion(targetPlugin);
                    String pluginAuthor = getPluginAuthor(targetPlugin);
                    int ramUsageMB = getRamUsage(targetPlugin);

                    // Reemplazar placeholders en cada regla y enviar el mensaje
                    for (String rule : rulesArray) {
                        String message = rule.replace("%plugin%", ChatColor.translateAlternateColorCodes('&', pluginName))
                                .replace("%version%", ChatColor.translateAlternateColorCodes('&', pluginVersion))
                                .replace("%ram%", String.valueOf(ramUsageMB))
                                .replace("%author%", ChatColor.translateAlternateColorCodes('&', pluginAuthor));

                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                    }
                } else {
                    sender.sendMessage(getMessages("messages.pluginNotFound").replace("%plugin%", targetPluginName));
                }

            } else {
                sender.sendMessage(getMessages("messages.fileNotFound"));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    private String getMessages(String formatKey) {
        if (messagesConfig.contains(formatKey)) {
            return ChatColor.translateAlternateColorCodes('&', messagesConfig.getString(formatKey));
        } else {
            return "<sender> whispers to <recipient>: <message>";
        }
    }

    private String getPluginVersion(Plugin targetPlugin) {
        return targetPlugin.getDescription().getVersion();
    }

    private String getPluginAuthor(Plugin targetPlugin) {
        List<String> authors = targetPlugin.getDescription().getAuthors();
        return authors.isEmpty() ? "Unknown Author" : authors.get(0);
    }

    private int getRamUsage(Plugin targetPlugin) {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();

        // Obtener la cantidad de memoria utilizada en megabytes
        long usedMemoryMB = heapMemoryUsage.getUsed() / (1024 * 1024);

        return (int) usedMemoryMB;
    }
}
