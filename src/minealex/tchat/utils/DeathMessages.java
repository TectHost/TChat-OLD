package minealex.tchat.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DeathMessages implements Listener {

	private final JavaPlugin plugin;
    private Map<String, String> deathMessages;
    private boolean deathMessagesEnabled;
    private boolean deathTitleEnabled;
    private String deathTitle;
    private String deathSubtitle;

    public DeathMessages(JavaPlugin plugin) {
        this.plugin = plugin;
        this.deathMessages = new HashMap<>();
        this.deathMessagesEnabled = true;
        this.deathTitleEnabled = true;
        this.deathTitle = "&cYou have died!";
        this.deathSubtitle = "&7Respawning in %time% seconds";
        loadDeathMessages();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void loadDeathMessages() {
        File configFile = new File(plugin.getDataFolder(), "death_messages.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Verifica si los mensajes de muerte están habilitados
        deathMessagesEnabled = config.getBoolean("enabled", true);

        // Si los mensajes de muerte están deshabilitados, no carga los mensajes
        if (!deathMessagesEnabled) {
            plugin.getLogger().warning("Death messages are disabled in the configuration. No custom death messages will be applied.");
            return;
        }

        // Cargar mensajes de muerte
        for (String deathType : config.getConfigurationSection("messages").getKeys(false)) {
            String message = ChatColor.translateAlternateColorCodes('&', config.getString("messages." + deathType));
            deathMessages.put(deathType, message);
        }

        // Cargar nombres personalizados de mobs
        if (config.contains("mob_names")) {
            for (String mobType : config.getConfigurationSection("mob_names").getKeys(false)) {
                String customMobName = ChatColor.translateAlternateColorCodes('&', config.getString("mob_names." + mobType));
                deathMessages.put("mob_names." + mobType, customMobName);
            }
        }

        // Cargar configuración de título y subtítulo
        deathTitleEnabled = config.getBoolean("death_title.enabled", true);
        deathTitle = ChatColor.translateAlternateColorCodes('&', config.getString("death_title.title", "&cYou have died!"));
        deathSubtitle = ChatColor.translateAlternateColorCodes('&', config.getString("death_title.subtitle", "&7Respawning in %time% seconds"));
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        // Verifica si los mensajes de muerte están habilitados
        if (!deathMessagesEnabled) {
            return;
        }

        Player player = event.getEntity();
        DamageCause damageCause = mapDamageCause(player.getLastDamageCause().getCause());
        String deathType = damageCause.name();

        EntityDamageEvent lastDamageCause = player.getLastDamageCause();
        String killerName = getKillerName(lastDamageCause);

        // Si no se puede determinar el atacante o no se encuentra un mensaje personalizado, usa el manejo anterior
        handleDeathMessage(event, deathType, player.getName(), killerName);

        // Muestra el título y subtítulo de la muerte
        if (deathTitleEnabled) {
            sendDeathTitle(player);
        }
    }

    private String getKillerName(EntityDamageEvent damageEvent) {
        Entity damager = null;

        if (damageEvent instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) damageEvent;
            damager = entityDamageByEntityEvent.getDamager();
        }

        if (damager != null) {
            if (damager instanceof Player) {
                return ((Player) damager).getName();
            } else if (damager instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) damager;

                // Obtener el nombre personalizado desde el mapa de nombres personalizados
                String customName = getMobName(livingEntity.getType(), Bukkit.getVersion());

                if (customName != null) {
                    return ChatColor.RESET + customName;
                } else {
                    return "Unknown";
                }
            }
        }

        return "Unknown";
    }

    @SuppressWarnings("deprecation")
    private void sendDeathTitle(Player player) {
        player.sendTitle(deathTitle, deathSubtitle);
    }
    
    private String getMobName(EntityType entityType, String minecraftVersion) {
        String customMobName = deathMessages.get("mob_names." + entityType.name());

        if (customMobName != null) {
            return customMobName;
        } else {
            switch (entityType) {
                case SKELETON:
                    return "Skeleton";
                case SPIDER:
                    return "Spider";
                case ZOMBIE:
                    return "Zombie";
                case GHAST:
                    return "Ghast";
                case SLIME:
                    return "Slime";
                case CREEPER:
                    return "Creeper";
                case PIG_ZOMBIE:
                    return "Pigman";
                case ENDERMAN:
                    return "Enderman";
                case CAVE_SPIDER:
                    return "Cave Spider";
                case SILVERFISH:
                    return "Silverfish";
                case BLAZE:
                    return "Blaze";
                case MAGMA_CUBE:
                    return "Magma Cube";
                case WITCH:
                    return "Witch";
                case ENDERMITE:
                    return "Endermite";
                case GUARDIAN:
                    return "Guardian";
                case WOLF:
                    return "Wolf";
                default:
                    return null;
            }
        }
    }

    private void handleDeathMessage(PlayerDeathEvent event, String deathType, String playerName, String killerName) {
        if (deathMessages.containsKey(deathType)) {
            String customDeathMessage = deathMessages.get(deathType)
                    .replace("%player%", playerName)
                    .replace("%killer%", killerName);
            event.setDeathMessage(customDeathMessage);
        } else {
            event.setDeathMessage(null);
        }
    }

    private DamageCause mapDamageCause(EntityDamageEvent.DamageCause cause) {
        switch (cause) {
            case ENTITY_ATTACK:
                return DamageCause.ENTITY_ATTACK;
            case FALL:
                return DamageCause.FALL;
            // Agrega más casos según sea necesario
            default:
                return cause;
        }
    }
}
