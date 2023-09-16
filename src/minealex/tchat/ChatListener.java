package minealex.tchat;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.clip.placeholderapi.PlaceholderAPI;
import minealex.tchat.blocked.AntiAdvertising;
import minealex.tchat.blocked.AntiCap;
import minealex.tchat.blocked.AntiFlood;
import minealex.tchat.blocked.AntiSpam;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.UnknownFormatConversionException;

@SuppressWarnings("unused")
public class ChatListener implements Listener {
    private TChat plugin;
    private AntiFlood antiFlood;
    private Map<UUID, Long> lastChatTime = new HashMap<>();
    private boolean isProcessingChat = false;
	private TChat antiAdvertising;

    public ChatListener(TChat plugin) {
        this.plugin = plugin;
        this.antiFlood = new AntiFlood(plugin.getChatCooldownSeconds());
        this.antiAdvertising = plugin;
    }

	@EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        
        message = message.replace("%", "%%");
        
        if (isAntispamEnabled() && AntiSpam.containsRepeatedLetters(message)) {
            event.setCancelled(true);
            AntiSpam.handleSpamMessage(player, message);
            return;
        }

        if (AntiSpam.containsRepeatedLetters(message)) {
            event.setCancelled(true);
            String antiSpamMessage = plugin.getMessage("antiSpamBlocked");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', antiSpamMessage));
            return;
        }
        
        if (isAntiAdvertisingEnabled()) {
            if (antiAdvertising.isAdvertisingBlocked(message)) {
                event.setCancelled(true);

                if (antiAdvertising.isIPv4Blocked() && message.matches(".*\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b.*")) {
                    antiAdvertising.handleBlockedIPv4(player);
                } else if (antiAdvertising.isDomainBlocked() && message.matches(".*\\b[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\\b.*")) {
                    antiAdvertising.handleBlockedDomain(player);
                } else if (antiAdvertising.isLinkBlocked() && message.matches(".*\\bhttps?://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}/?.*")) {
                    antiAdvertising.handleBlockedLink(player);
                }
                return;
            }
        }
        
        if (!antiFlood.canPlayerChat(player)) {
            event.setCancelled(true);
            int remainingTime = antiFlood.getRemainingTime(player);
            String message1 = plugin.getMessage("chatCooldownMessage");

            // Reemplaza el marcador de posición %time% con el tiempo restante
            message1 = message1.replace("%time%", String.valueOf(remainingTime));

            // Aplica colores y establece el mensaje personalizado
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message1));
            return;
        }

        // Restricción del tiempo entre mensajes
        long currentTimeMillis = System.currentTimeMillis();
        long lastChatMillis = lastChatTime.getOrDefault(player.getUniqueId(), 0L);
        int chatCooldownSeconds = plugin.getChatCooldownSeconds();
        String chatCooldownMessage = plugin.getMessage("chatCooldownMessage"); 

        if (currentTimeMillis - lastChatMillis < chatCooldownSeconds * 1000) {
            event.setCancelled(true);
            int remainingTime = (int) (chatCooldownSeconds - (currentTimeMillis - lastChatMillis) / 1000);
            String message1 = chatCooldownMessage.replace("%time%", String.valueOf(remainingTime));

            // Aplicar colores y enviar el mensaje personalizado
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message1));
            return;
        }

        // Actualizar el tiempo del último mensaje
        lastChatTime.put(player.getUniqueId(), currentTimeMillis);
        
        if (isAnticapEnabled()) {
            String correctedMessage = AntiCap.fixCaps(message);
            String format1 = plugin.formatMessage(correctedMessage, event.getPlayer());
            format1 = PlaceholderAPI.setPlaceholders(player, format1);
            event.setFormat(format1);
            event.setMessage(correctedMessage.replace("%", "%%"));
        } else {
            String format = plugin.formatMessage(message, event.getPlayer());
            format = PlaceholderAPI.setPlaceholders(player, format);
            event.setFormat(format);
            event.setMessage(message.replace("%", "%%"));
        }

        // Check if the message contains any banned words
        if (plugin.getBannedWords().isWordBanned(message.toLowerCase()) && !plugin.getBannedWords().canBypassBannedWords(player)) {
            plugin.getBannedWords().sendBlockedMessage(player);
            event.setCancelled(true);
            return;
        }
        UUID playerId = player.getUniqueId();
			if (!plugin.hasPlayerMoved(playerId)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Debes moverte antes de poder chatear.");
                return;
            }
        // Format the message and set the chat format
        String format = plugin.formatMessage(event.getMessage(), event.getPlayer());
        format = PlaceholderAPI.setPlaceholders(player, format);
        event.setFormat(format);
        
        if (isAnticapEnabled()) {
            // Aplicar anticap solo si está habilitado
            String correctedMessage = AntiCap.fixCaps(message);

            // Format the message and set the chat format
            String format1 = plugin.formatMessage(correctedMessage, event.getPlayer());
            event.setFormat(format1);
            event.setMessage(correctedMessage);
        }
    }

	private boolean isAntiAdvertisingEnabled() {
        return plugin.isAntiAdvertisingEnabled();
    }
	
	private boolean isDomainBlocked() {
		return false;
	}

	private boolean isIPv4Blocked() {
		return false;
	}

	private boolean isAntispamEnabled() {
		return false;
	}

	private int getChatCooldownSeconds() {
		return 0;
	}

	private boolean isAnticapEnabled() {
        File configFile = new File(plugin.getDataFolder(), "format_config.json");
        if (!configFile.exists()) {
            return false; // Si el archivo no existe, la función está deshabilitada por defecto.
        }

        try (FileReader reader = new FileReader(configFile)) {
            JsonParser parser = new JsonParser();
            JsonObject jsonObject = parser.parse(reader).getAsJsonObject();

            return jsonObject.get("anticap_enabled").getAsBoolean(); // Leer anticap_enabled del JSON
        } catch (IOException e) {
            plugin.getLogger().warning("Error reading format_config.json: " + e.getMessage());
        } catch (Exception e) {
            plugin.getLogger().warning("Error reading anticap_enabled from format_config.json: " + e.getMessage());
        }

        return false; // En caso de error, asumimos que la función está deshabilitada.
    }

	// Agrega un método para verificar si el jugador se ha movido
    private boolean hasPlayerMoved(Player player) {
        Location lastLocation = plugin.getLastPlayerLocation(player);

        // Verificar si la ubicación actual es la misma que la última conocida
        if (lastLocation == null || !lastLocation.equals(player.getLocation())) {
            // Actualizar la última ubicación conocida
            plugin.setLastPlayerLocation(player, player.getLocation());
            return true;
        }

        return false;
    }
}
