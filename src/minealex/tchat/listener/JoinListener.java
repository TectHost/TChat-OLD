package minealex.tchat.listener;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;

import minealex.tchat.TChat;

public class JoinListener implements Listener {

    private final TChat plugin;

    public JoinListener(TChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        try {
            Player player = event.getPlayer();
            String playerName = player.getName();

            String groupName = getPrimaryGroup(player);
            ConfigurationSection config = plugin.getConfig().getConfigurationSection("Joins." + groupName);
            if (config == null) {
                config = plugin.getConfig().getConfigurationSection("Joins.default");
            }

            // Handling join message
            boolean joinMessagesEnabled = config.getBoolean("joinMessagesEnabled", true);
            if (joinMessagesEnabled) {
            	String joinMessage = config.getString("JoinMessage");
            	if (joinMessage != null && !joinMessage.isEmpty()) {
            	    joinMessage = ChatColor.translateAlternateColorCodes('&', joinMessage.replace("%player%", playerName));
            	    event.setJoinMessage(joinMessage);
            	}
            }

            // Handling entry commands
            boolean entryCommandsEnabled = config.getBoolean("entryCommandsEnabled", true);
            if (entryCommandsEnabled) {
                executeEntryCommands(playerName, config.getStringList("entryCommands"));
            }

            // Handling particles
            spawnParticles(player.getLocation(), config.getString("Particles.Join.joinParticlesType"));

            // Handling sounds
            playJoinSound(player.getLocation());

            // Handling titles
            sendJoinTitle(player);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        try {
            Player player = event.getPlayer();
            String playerName = player.getName();

            String groupName = getPrimaryGroup(player);
            ConfigurationSection config = plugin.getConfig().getConfigurationSection("Joins." + groupName);
            if (config == null) {
                config = plugin.getConfig().getConfigurationSection("Joins.default");
            }

            // Handling quit message
            boolean quitMessagesEnabled = config.getBoolean("quitMessagesEnabled", true);
            if (quitMessagesEnabled) {
            	String quitMessage = config.getString("QuitMessage");
            	if (quitMessage != null && !quitMessage.isEmpty()) {
            	    quitMessage = ChatColor.translateAlternateColorCodes('&', quitMessage.replace("%player%", playerName));
            	    event.setQuitMessage(quitMessage);
            	}
            }

            // Handling quit commands
            boolean quitCommandsEnabled = config.getBoolean("quitCommandsEnabled", true);
            if (quitCommandsEnabled) {
                executeQuitCommands(playerName, config.getStringList("quitCommands"));
            }

            // Handling particles
            spawnParticles(player.getLocation(), config.getString("Particles.Quit.quitParticlesType"));

            // Handling sounds
            playQuitSound(player.getLocation());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getPrimaryGroup(Player player) {
        // Si el jugador es op, devuelve el grupo "op"
        if (player.isOp()) {
            return "op";
        }

        // Obtener todos los grupos definidos en la configuración
        ConfigurationSection joinGroups = plugin.getConfig().getConfigurationSection("JoinGroups");
        if (joinGroups == null) {
            return "default";
        }

        // Verificar si el jugador tiene permiso para unirse a algún grupo
        for (String groupName : joinGroups.getKeys(false)) {
            if (player.hasPermission("tchat.join." + groupName)) {
                return groupName;
            }
        }

        // Si no tiene permiso para ningún grupo específico, devuelve el grupo predeterminado
        return "default";
    }

    private void executeEntryCommands(String playerName, List<String> commands) {
        for (String command : commands) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", playerName));
        }
    }

    private void executeQuitCommands(String playerName, List<String> commands) {
        for (String command : commands) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", playerName));
        }
    }

    private void spawnParticles(Location location, String particleType) {
        try {
            Effect effect = Effect.valueOf(particleType.toUpperCase());
            location.getWorld().playEffect(location, effect, 0);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid particle type: " + particleType);
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

    private void sendJoinTitle(Player player) {
        FileConfiguration config = plugin.getConfig();
        String groupName = getPrimaryGroup(player);
        ConfigurationSection groupConfig = config.getConfigurationSection("Joins." + groupName);
        if (groupConfig == null) {
            groupConfig = config.getConfigurationSection("Joins.default");
        }

        if (groupConfig.contains("Titles.enabled") && groupConfig.getBoolean("Titles.enabled")) {
            String title = groupConfig.getString("Titles.title");
            String subtitle = groupConfig.getString("Titles.subtitle");
            boolean sendOnlyToJoiningPlayer = groupConfig.getBoolean("Titles.sendOnlyToJoiningPlayer");
            
            title = title.replace("%player%", player.getName());
            subtitle = subtitle.replace("%player%", player.getName());

            if (sendOnlyToJoiningPlayer) {
                sendTitle(player, title, subtitle);
            } else {
                // Send title and subtitle to all online players
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    sendTitle(onlinePlayer, title, subtitle);
                }
            }
        }
    }

    private void sendTitle(Player player, String title, String subtitle) {
        new BukkitRunnable() {
            @SuppressWarnings("deprecation")
            @Override
            public void run() {
                player.sendTitle(ChatColor.translateAlternateColorCodes('&', title), ChatColor.translateAlternateColorCodes('&', subtitle));
            }
        }.runTaskLater(plugin, 20); // Delayed by 1 second (20 ticks)
    }
}
