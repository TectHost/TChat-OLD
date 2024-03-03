package minealex.tchat.listener;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.nio.charset.StandardCharsets;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;

import me.clip.placeholderapi.PlaceholderAPI;

import minealex.tchat.TChat;

public class JoinListener implements Listener {

    private final TChat plugin;

    public JoinListener(TChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        try {
            boolean motdEnabled = isMotdEnabled();
            if (motdEnabled) {
                // Enviar el mensaje de MOTD al jugador
                sendMotdMessage(event.getPlayer().getName());
            }

            // Obtener y configurar el mensaje de join
            String joinMessage = getConfiguredJoinMessage(event.getPlayer().getName());
            if (joinMessage != null) {
                event.setJoinMessage(joinMessage);
            }
            executeEntryCommands(event.getPlayer().getName());

            spawnParticles(event.getPlayer().getLocation(), "Join");
            
            playJoinSound(event.getPlayer().getLocation());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        try {
            // Get and set the quit message
            String quitMessage = getConfiguredQuitMessage(event.getPlayer().getName());
            if (quitMessage != null) {
                event.setQuitMessage(quitMessage);
            }
            executeQuitCommands(event.getPlayer().getName());

            spawnParticles(event.getPlayer().getLocation(), "Quit");
            
            playQuitSound(event.getPlayer().getLocation());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isMotdEnabled() {
        FileConfiguration config = plugin.getConfig();
        return config.getBoolean("Motd.motdEnabled", true);
    }
    
    @SuppressWarnings("deprecation")
	private void executeEntryCommands(String playerName) {
        FileConfiguration config = plugin.getConfig();
        if (config.contains("Joins.entryCommandsEnabled") && config.getBoolean("Joins.entryCommandsEnabled")) {
            if (config.contains("Joins.entryCommands")) {
                for (String command : config.getStringList("Joins.entryCommands")) {
                    // Ejecutar cada comando de la lista de comandos de entrada
                    command = command.replace("%player%", playerName);
                    command = PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(playerName), command);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                }
            }
        }
    }
    
    @SuppressWarnings("deprecation")
	private void executeQuitCommands(String playerName) {
        FileConfiguration config = plugin.getConfig();
        if (config.contains("Joins.quitCommandsEnabled") && config.getBoolean("Joins.quitCommandsEnabled")) {
            if (config.contains("Joins.quitCommands")) {
                for (String command : config.getStringList("Joins.quitCommands")) {
                    // Ejecutar cada comando de la lista de comandos de salida
                    command = command.replace("%player%", playerName);
                    command = PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(playerName), command);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
	private String getConfiguredJoinMessage(String playerName) {
        FileConfiguration config = plugin.getConfig();
        boolean joinMessagesEnabled = config.getBoolean("Joins.joinMessagesEnabled", true);

        if (!joinMessagesEnabled) {
            return null;
        }

        String joinMessage = config.getString("Joins.JoinMessage");
        if (joinMessage != null) {
            joinMessage = joinMessage.replace("%player%", playerName);
            joinMessage = PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(playerName), joinMessage);
            joinMessage = new String(joinMessage.getBytes(), StandardCharsets.UTF_8); // Asegurar la codificación
            return ChatColor.translateAlternateColorCodes('&', joinMessage);
        }

        return null;
    }

    @SuppressWarnings("deprecation")
	private String getConfiguredQuitMessage(String playerName) {
        FileConfiguration config = plugin.getConfig();
        boolean quitMessagesEnabled = config.getBoolean("Joins.quitMessagesEnabled", true);

        if (!quitMessagesEnabled) {
            return null;
        }

        String quitMessage = config.getString("Joins.QuitMessage");
        if (quitMessage != null) {
            quitMessage = quitMessage.replace("%player%", playerName);
            quitMessage = PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(playerName), quitMessage);
            quitMessage = new String(quitMessage.getBytes(), StandardCharsets.UTF_8); // Asegurar la codificación
            return ChatColor.translateAlternateColorCodes('&', quitMessage);
        }

        return null;
    }


    private void sendMotdMessage(String playerName) {
        FileConfiguration config = plugin.getConfig();
        if (config.contains("Motd.motdEnabled") && config.getBoolean("Motd.motdEnabled")) {
            if (config.contains("Motd.motdMessage")) {
                for (String message : config.getStringList("Motd.motdMessage")) {
                    // Send each line of the MOTD message to the player
                    String formattedMessage = ChatColor.translateAlternateColorCodes('&', message);
                    Bukkit.getPlayer(playerName).sendMessage(formattedMessage);
                }
            }
        }
    }
    
    @SuppressWarnings("deprecation")
	private void spawnParticles(Location location, String eventType) {
        FileConfiguration config = plugin.getConfig();
        String particlesTypeKey = "Joins.Particles." + eventType.toLowerCase() + "ParticlesType";
        String particlesEnabledKey = "Joins.Particles." + eventType.toLowerCase() + "ParticlesEnabled";

        if (config.contains(particlesTypeKey) && config.getBoolean(particlesEnabledKey)) {
            String particlesType = config.getString(particlesTypeKey);
            int effectId = getEffectId(particlesType);
            
            if (effectId != -1) {
                location.getWorld().playEffect(location, Effect.getById(effectId), 0);
            }
        }
    }

    private int getEffectId(String particlesType) {
        switch (particlesType.toUpperCase()) {
            case "FLAME":
                return 2001;
            case "CLOUD":
                return 2004;
            default:
                return -1;
        }
    }
    
    private void playJoinSound(Location location) {
        FileConfiguration config = plugin.getConfig();
        String soundEnabledKey = "Joins.Sounds.Join.joinSoundEnabled";
        String soundTypeKey = "Joins.Sounds.Join.joinSoundType";

        if (config.contains(soundEnabledKey) && config.getBoolean(soundEnabledKey)) {
            String soundType = config.getString(soundTypeKey);
            playSound(location, soundType);
        }
    }

    private void playQuitSound(Location location) {
        FileConfiguration config = plugin.getConfig();
        String soundEnabledKey = "Joins.Sounds.Quit.quitSoundEnabled";
        String soundTypeKey = "Joins.Sounds.Quit.quitSoundType";

        if (config.contains(soundEnabledKey) && config.getBoolean(soundEnabledKey)) {
            String soundType = config.getString(soundTypeKey);
            playSound(location, soundType);
        }
    }

    private void playSound(Location location, String soundType) {
        try {
            Sound sound = Sound.valueOf(soundType.toUpperCase());
            location.getWorld().playSound(location, sound, 1, 1);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound type: " + soundType);
        }
    }
}
