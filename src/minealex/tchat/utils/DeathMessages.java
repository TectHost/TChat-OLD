package minealex.tchat.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class DeathMessages implements Listener {

    private final JavaPlugin plugin;
    private Map<String, String> deathMessages;
    private boolean deathMessagesEnabled;

    public DeathMessages(JavaPlugin plugin) {
        this.plugin = plugin;
        this.deathMessages = new HashMap<>();
        this.deathMessagesEnabled = true;
        loadDeathMessages();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void loadDeathMessages() {
        File configFile = new File(plugin.getDataFolder(), "death_messages.json");

        if (!configFile.exists()) {
            plugin.getLogger().warning("death_messages.json not found. No custom death messages will be applied.");
            return;
        }

        try (FileReader reader = new FileReader(configFile)) {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);

            // Verifica si los mensajes de muerte están habilitados
            if (jsonObject.has("enabled")) {
                deathMessagesEnabled = jsonObject.get("enabled").getAsBoolean();
            }

            // Si los mensajes de muerte están deshabilitados, no carga los mensajes
            if (!deathMessagesEnabled) {
                plugin.getLogger().warning("Death messages are disabled in the configuration. No custom death messages will be applied.");
                return;
            }

            JsonObject messagesObject = jsonObject.getAsJsonObject("messages");

            for (Map.Entry<String, JsonElement> entry : messagesObject.entrySet()) {
                String key = entry.getKey();
                String message = ChatColor.translateAlternateColorCodes('&', entry.getValue().getAsString());
                deathMessages.put(key, message);
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Error loading death_messages.json", e);
        }
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
                String customName = livingEntity.getCustomName();

                if (customName != null) {
                    return ChatColor.RESET + customName;
                } else {
                    // Si la entidad no tiene un nombre personalizado, puedes usar su tipo como fallback
                    EntityType entityType = livingEntity.getType();
                    String minecraftVersion = Bukkit.getVersion();
                    String mobName = getMobName(entityType, minecraftVersion);

                    if (mobName != null) {
                        return ChatColor.RESET + mobName;
                    } else {
                        return "Unknown";
                    }
                }
            }
        }

        return "Unknown";
    }

    private String getMobName(EntityType entityType, String minecraftVersion) {
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
                return null; // Devuelve null para indicar que no se encontró un nombre específico
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
