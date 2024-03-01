package minealex.tchat.utils;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Levels {

    private Plugin plugin;

    public Levels(Plugin plugin) {
        this.plugin = plugin;
        createConfig();
    }

    private FileConfiguration getLevelsConfig() {
        return YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "levels.yml"));
    }

    public void addExperience(Player player) {
        int minExp = getMinExperience();
        int maxExp = getMaxExperience();

        // Obtener la experiencia actual del jugador desde saves.yml
        int currentExp = getExperience(player);

        // Generar un número aleatorio entre minExp y maxExp
        int randomExp = minExp + (int) (Math.random() * ((maxExp - minExp) + 1));

        // Sumar la experiencia aleatoria al total acumulado y actualizar saves.yml
        int newTotalExp = currentExp + randomExp;
        setExperience(player, newTotalExp);

        // Verificar si el jugador ha alcanzado un nuevo nivel
        checkLevelUp(player, newTotalExp);
    }

    private int getMinExperience() {
        return getLevelsConfig().getInt("min_experience", 1);
    }

    private int getMaxExperience() {
        return getLevelsConfig().getInt("max_experience", 7);
    }

    public int getExperience(Player player) {
        FileConfiguration savesConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "saves.yml"));
        return savesConfig.getInt("players." + player.getUniqueId().toString() + ".experience", 0);
    }

    private void setExperience(Player player, int experience) {
        File savesFile = new File(plugin.getDataFolder(), "saves.yml");
        FileConfiguration savesConfig = YamlConfiguration.loadConfiguration(savesFile);

        String playerUUID = player.getUniqueId().toString();
        String path = "players." + playerUUID;

        // Asegurémonos de que la sección 'players' exista
        if (!savesConfig.isConfigurationSection("players")) {
            savesConfig.createSection("players");
        }

        // Asegurémonos de que la sección del jugador exista
        if (!savesConfig.isConfigurationSection(path)) {
            savesConfig.createSection(path);
        }

        // Establecer la experiencia actualizada del jugador, sin límite máximo
        savesConfig.set(path + ".experience", experience);

        try {
            savesConfig.save(savesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkLevelUp(Player player, int newTotalExp) {
        FileConfiguration savesConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "saves.yml"));
        String playerUUID = player.getUniqueId().toString();
        int currentLevel = savesConfig.getInt("players." + playerUUID + ".level", 0);

        ConfigurationSection levelsSection = getLevelsConfig().getConfigurationSection("");
        for (String levelKey : levelsSection.getKeys(false)) {
            if (levelKey.startsWith("level")) {
                int levelNumber = Integer.parseInt(levelKey.substring(5));
                int requiredExp = levelsSection.getInt(levelKey + ".xp", 0);

                if (newTotalExp >= requiredExp && currentLevel < levelNumber) {
                    List<String> commands = levelsSection.getStringList(levelKey + ".rewards");
                    for (String command : commands) {
                        String formattedCommand = command.replace("%player%", player.getName());

                        // Ejecutar el comando en el hilo principal
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formattedCommand);
                        });
                    }

                    savesConfig.set("players." + playerUUID + ".level", levelNumber);

                    try {
                        savesConfig.save(new File(plugin.getDataFolder(), "saves.yml"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void createConfig() {
        File levelsFile = new File(plugin.getDataFolder(), "levels.yml");

        if (!levelsFile.exists()) {
            plugin.saveResource("levels.yml", false);
        }

        File savesFile = new File(plugin.getDataFolder(), "saves.yml");

        if (!savesFile.exists()) {
            try {
                savesFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
