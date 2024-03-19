package minealex.tchat.bot;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class CommandTimer {

    private final JavaPlugin plugin;
    private final File configFile;
    private FileConfiguration config;

    public CommandTimer(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "command_timer.yml");
        if (!configFile.exists()) {
            plugin.saveResource("command_timer.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(configFile);
        startTimers();
    }

    private void startTimers() {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            ConfigurationSection commandsSection = config.getConfigurationSection("commands");
            if (commandsSection != null) {
                for (String key : commandsSection.getKeys(false)) {
                    ConfigurationSection commandSection = commandsSection.getConfigurationSection(key);
                    if (commandSection != null && commandSection.getBoolean("enabled")) {
                        String command = commandSection.getString("command");
                        int time = commandSection.getInt("time");
                        Bukkit.getScheduler().runTaskTimer(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command), 0L, time * 20L);
                    }
                }
            }
        }, 20L); // Retraso de 1 segundo (20 ticks)
    }

    public void reloadConfig() {
        this.config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
