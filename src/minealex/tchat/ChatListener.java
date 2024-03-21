package minealex.tchat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachment;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.clip.placeholderapi.PlaceholderAPI;
import minealex.tchat.blocked.AntiAdvertising;
import minealex.tchat.blocked.AntiCap;
import minealex.tchat.blocked.AntiFlood;
import minealex.tchat.blocked.AntiSpam;
import minealex.tchat.blocked.BannedCommands;
import minealex.tchat.bot.ChatBot;
import minealex.tchat.bot.ChatGames;
import minealex.tchat.perworldchat.PerWorldChat;
import minealex.tchat.perworldchat.WorldConfig;
import minealex.tchat.perworldchat.WorldsManager;
import minealex.tchat.utils.Levels;
import minealex.tchat.utils.HoverGroup;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.UnknownFormatConversionException;
import java.util.logging.Level;

@SuppressWarnings("unused")
public class ChatListener implements Listener {
    private TChat plugin;
    private AntiFlood antiFlood;
    private Map<UUID, Long> lastChatTime = new HashMap<>();
    private boolean isProcessingChat = false;
	private TChat antiAdvertising;
	private ChatGames chatGames;
	private JSONObject chatbotRespuestas;
	private BannedCommands bannedCommands;
	private String staffChatFormat;
	private PerWorldChat perWorldChat;
	private List<String> hoverText;
	private Levels levels;
	private Map<String, HoverGroup> hoverGroups;

    public ChatListener(TChat plugin) {
        this.plugin = plugin;
        this.antiFlood = new AntiFlood(plugin.getChatCooldownSeconds());
        this.antiAdvertising = plugin;
        this.chatGames = plugin.getChatGames();
        this.bannedCommands = new BannedCommands(plugin);
        this.perWorldChat = new PerWorldChat(plugin);
        this.staffChatFormat = loadStaffChatFormatFromConfig();
        this.hoverText = new ArrayList<>();
        this.levels = new Levels(plugin);
        loadHoverGroupsFromConfig();
    }
    
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String[] commandArgs = event.getMessage().split(" ");
        String command = commandArgs[0].substring(1).toLowerCase();

        Player player = event.getPlayer();

        if (bannedCommands.canBypassCommandBlocker(player)) {
            return;
        }
        
        if (bannedCommands.isCommandBanned(command)) {
            event.setCancelled(true);
            Player sender = event.getPlayer();
            String blockedMessage = bannedCommands.loadBlockedMessage();
            bannedCommands.sendTitle((Player) sender);
            bannedCommands.playSound((Player) sender);
            bannedCommands.executeCommandsOnBlock((Player) sender, command);
            player.sendMessage(blockedMessage);
            
            plugin.getLogger().warning("Player " + player.getName() + " attempted to execute blocked command: /" + command);
        }
    }
    
    private boolean isUnicodeBlocked() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            return false; // Si el archivo no existe, asumimos que la función está deshabilitada por defecto.
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Obtener el valor de Other.anti_unicode de config.yml
        if (config.contains("Other.anti_unicode")) {
            return config.getBoolean("Other.anti_unicode");
        } else {
            plugin.getLogger().warning("Other.anti_unicode not found in config.yml.");
        }

        return false; // En caso de error, asumimos que la función está deshabilitada.
    }
    
	@EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        String worldName = player.getWorld().getName();
        
        message = message.replace("%", "%%");
        
        levels.addExperience(player);
        
        ChatBot chatBot = plugin.getChatBot();
        chatBot.sendResponse(message, player);
        message = ChatColor.translateAlternateColorCodes('&', message);
        
        if (player.hasPermission("tchat.mention")) {
        	String[] words = message.split(" ");

        	for (int i = 0; i < words.length; i++) {
        		if (words[i].startsWith("@")) {
        			String mentionedPlayerName = words[i].substring(1); // Eliminar el carácter '@'
        			Player mentionedPlayer = Bukkit.getPlayer(mentionedPlayerName);

        			if (mentionedPlayer != null) {
        				// Reemplazar la mención con el nombre del jugador en color morado
        				words[i] = ChatColor.LIGHT_PURPLE + "@" + mentionedPlayer.getName() + ChatColor.RESET;
                    
        				// Reproducir el sonido para el jugador mencionado
        				String soundPath = plugin.getConfig().getString("mention_sound");
        				if (soundPath != null && !soundPath.isEmpty()) {
        					Sound mentionSound = Sound.valueOf(soundPath);
        					mentionedPlayer.playSound(mentionedPlayer.getLocation(), mentionSound, 1, 1);
        				}
                    }
                }
            }

        String modifiedMessage = ChatColor.translateAlternateColorCodes('&', String.join(" ", words));
        event.setMessage(modifiedMessage);
        }
        
        WorldsManager worldsManager = plugin.getWorldsManager();
        WorldConfig worldConfig = worldsManager.loadWorldConfig(worldName);

        if (worldConfig.isRadiusChatEnabled()) {
            int radius = worldConfig.getRadiusChat();
            
            event.getRecipients().removeIf(recipient -> {
                Location playerLocation = player.getLocation();
                Location recipientLocation = recipient.getLocation();
                
                double distanceSquared = playerLocation.distanceSquared(recipientLocation);
                double radiusSquared = radius * radius;
                
                return distanceSquared > radiusSquared;
            });
        }
        
        message = message.replace("[hand]", getItemText(player.getItemInHand()));
        
        List<String> ignoredPlayersSender = plugin.getConfig().getStringList("players." + player.getUniqueId() + ".ignore");
        List<Player> nuevosDestinatarios = new ArrayList<>();

        for (Player recipient : event.getRecipients()) {
            List<String> ignoredPlayersRecipient = plugin.getConfig().getStringList("players." + recipient.getUniqueId() + ".ignore");

            if (!(ignoredPlayersSender.contains(recipient.getName()) || ignoredPlayersRecipient.contains(player.getName()))) {
                nuevosDestinatarios.add(recipient);
            }
        }

        event.getRecipients().clear();
        event.getRecipients().addAll(nuevosDestinatarios);
        
        if (player.hasPermission("tchat.color")) {
            // Si tiene el permiso, reemplazar los colores &
            message = ChatColor.translateAlternateColorCodes('&', message);
        } else {
            // Si no tiene el permiso, no aplicar colores
            message = ChatColor.stripColor(message); // Elimina cualquier código de color
        }
        
        String[] words1 = message.split("[^a-zA-Z0-9_]+");
        for (String word : words1) {
            // Verificar si el jugador tiene el permiso de bypass o es operador
            if (plugin.getBannedWords().canBypassBannedWords(player)) {
                // No realizar ninguna acción adicional si el jugador puede bypass
            } else {
                if (!word.isEmpty() && plugin.getBannedWords().isWordBanned(word)) {
                    plugin.getBannedWords().sendBlockedMessage(player, word);
                    plugin.getBannedWords().executeConsoleCommands(player);
                    event.setCancelled(true);
                    return;
                }
            }
        }
        
        if (plugin.getStaffChatPlayers().contains(player.getUniqueId())) {
            event.setCancelled(true);

            // Envía el mensaje del staff chat a los demás jugadores de staff
            for (UUID staffMember : plugin.getStaffChatPlayers()) {
                Player staffPlayer = plugin.getServer().getPlayer(staffMember);
                if (staffPlayer != null) {
                    staffPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', staffChatFormat
                            .replace("%player%", player.getName())
                            .replace("%message%", message)
                    ));
                }
            }
        } else {
        	try {
        	    event.setFormat(plugin.formatMessage(message, player));
        	    message = PlaceholderAPI.setPlaceholders(player, message);
        	} catch (Exception e) {
        	    e.printStackTrace();
        	}
        }
        
        if (chatbotRespuestas != null && chatbotRespuestas.containsKey(message.toLowerCase())) {
            String respuesta = (String) chatbotRespuestas.get(message.toLowerCase());
            player.sendMessage(ChatColor.GREEN + "Chatbot dice: " + ChatColor.RESET + respuesta);
        }
        
        if (isUnicodeBlocked() && !player.hasPermission("tchat.bypass.unicode") && containsEmojiOrUnicode(message)) {
            event.setCancelled(true);
            String antiUnicodeMessage = plugin.getMessagesYML("messages.antiUnicodeBlocked");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', antiUnicodeMessage));
            return;
        }
        
        if (!player.hasPermission("tchat.bypass.antispam")) {
            if (isAntispamEnabled() && AntiSpam.containsRepeatedLetters(message)) {
                event.setCancelled(true);
                AntiSpam.handleSpamMessage(player, message);
                return;
            }

            if (AntiSpam.containsRepeatedLetters(message)) {
                event.setCancelled(true);
                String antiSpamMessage = plugin.getMessagesYML("messages.antiSpamBlocked");
                plugin.getLogger().warning("Spam detected for player: " + player.getName());
                player.sendMessage(antiSpamMessage);
                return;
            }
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
        
        if (!player.hasPermission("tchat.bypass.antiflood")) {
        	if (!antiFlood.canPlayerChat(player)) {
        		event.setCancelled(true);
        		int remainingTime = antiFlood.getRemainingTime(player);
        		String message1 = plugin.getMessagesYML("messages.chatCooldownMessage");

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
        	String chatCooldownMessage = plugin.getMessagesYML("messages.chatCooldownMessage"); 

        	if (currentTimeMillis - lastChatMillis < chatCooldownSeconds * 1000) {
        		event.setCancelled(true);
        		int remainingTime = (int) (chatCooldownSeconds - (currentTimeMillis - lastChatMillis) / 1000);
        		String message1 = chatCooldownMessage.replace("%time%", String.valueOf(remainingTime));

        		// Aplicar colores y enviar el mensaje personalizado
        		player.sendMessage(ChatColor.translateAlternateColorCodes('&', message1));
        		plugin.getLogger().warning("Flood detected for player: " + player.getName());
        		return;
        	}

        // Actualizar el tiempo del último mensaje
        lastChatTime.put(player.getUniqueId(), currentTimeMillis);
        }
        
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
        
        UUID playerId = player.getUniqueId();
			if (!plugin.hasPlayerMoved(playerId)) {
                event.setCancelled(true);
                String antiBotMessage = plugin.getMessagesYML("messages.antiBotMessage");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', antiBotMessage));
                return;
            }

        String format = plugin.formatMessage(event.getMessage(), event.getPlayer());
        format = PlaceholderAPI.setPlaceholders(player, format);
        event.setFormat(format);
        
        chatGames.processChat(player, message);
        
        if (isAnticapEnabled()) {
            // Aplicar anticap solo si está habilitado
            String correctedMessage = AntiCap.fixCaps(message);

            // Format the message and set the chat format
            String format1 = plugin.formatMessage(correctedMessage, event.getPlayer());
            event.setFormat(format1);
            event.setMessage(correctedMessage);
        }
        
        String playerNameFormat = getPlayerNameFormat(player);
        if (playerNameFormat != null && isHoverTextEnabled(player)) {
            playerNameFormat = PlaceholderAPI.setPlaceholders(player, playerNameFormat);

            String groupName = getPrimaryGroup(player);
            HoverGroup hoverGroup = hoverGroups.get(groupName);
            
            if (plugin.getStaffChatPlayers().contains(player.getUniqueId())) {
                // Si está en el chat del staff, cancelar el evento y salir del método
                event.setCancelled(true);
                return;
            }

            if (hoverGroup != null && hoverGroup.isEnabled()) {
                String formattedName = ChatColor.translateAlternateColorCodes('&', playerNameFormat);
                TextComponent playerName = new TextComponent(formattedName);

                TextComponent message1 = new TextComponent(event.getMessage());

                playerName.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, hoverGroup.getSuggestCommand().replace("%player%", player.getName())));
                playerName.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent(ChatColor.translateAlternateColorCodes('&', String.join("\n", hoverGroup.getHoverText())))}));

                TextComponent finalMessage = new TextComponent(playerName);
                finalMessage.addExtra(" ");
                finalMessage.addExtra(message1);

                Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
                    // ... Verifica si el jugador está en staffchat ...
                    onlinePlayer.spigot().sendMessage(finalMessage);
                });

                Bukkit.getServer().getConsoleSender().sendMessage(finalMessage.toLegacyText());

                event.setCancelled(true);
            }
        }
    }
	
	private boolean isHoverTextEnabled(Player player) {
	    String groupName = getPrimaryGroup(player);
	    if (hoverGroups.containsKey(groupName) && hoverGroups.get(groupName).isEnabled()) {
	        return true;
	    } else {
	        plugin.getLogger().info("No HoverGroup found for group: " + groupName);
	        return false;
	    }
	}
	
	private String getPrimaryGroup(Player player) {
	    PermissionAttachment attachment = player.addAttachment(plugin);
	    String primaryGroup = null;
	    
	    if (player.isOp()) {
	        return "op";
	    }

	    for (String groupName : hoverGroups.keySet()) {
	        if (player.hasPermission("tchat.hover." + groupName)) {
	            primaryGroup = groupName;
	            break;
	        }
	    }

	    player.removeAttachment(attachment);
	    return primaryGroup != null ? primaryGroup : "default";
	}

	private boolean isAntiAdvertisingEnabled() {
        return plugin.isAntiAdvertisingEnabled();
    }
	
	private boolean isDomainBlocked() {
		return false;
	}
	
	private String getPlayerNameFormat(Player player) {
	    File configFile = new File(plugin.getDataFolder(), "format_config.json");

	    if (!configFile.exists()) {
	        plugin.getLogger().warning("format_config.json not found. Using default player name format.");
	        return "&a%tchat_nickname%";
	    }

	    try {
	        JSONParser parser = new JSONParser();
	        Object obj = parser.parse(new FileReader(configFile));
	        
	        if (obj instanceof JSONObject) {
	            JSONObject jsonConfig = (JSONObject) obj;

	            if (jsonConfig.containsKey("format")) {
	                return ChatColor.translateAlternateColorCodes('&', (String) jsonConfig.get("format"));
	            } else {
	                plugin.getLogger().warning("format not found in format_config.json. Using default player name format.");
	                return "&a%tchat_nickname%";
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        plugin.getLogger().warning("Error reading format_config.json. Using default player name format.");
	    }

	    return "&a%tchat_nickname%";
	}

	
	private boolean isAntiUnicodeEnabled() {
	    File configFile = new File(plugin.getDataFolder(), "config.yml");
	    if (!configFile.exists()) {
	        return false;
	    }

	    FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

	    // Obtener el valor de Other.anti_unicode de config.yml
	    if (config.contains("Other.anti_unicode")) {
	        return config.getBoolean("Other.anti_unicode");
	    } else {
	        plugin.getLogger().warning("Other.anti_unicode not found in config.yml.");
	    }

	    return false;
	}
	
	private boolean containsEmojiOrUnicode(String message) {
	    for (char c : message.toCharArray()) {
	        if (Character.UnicodeBlock.of(c) != Character.UnicodeBlock.BASIC_LATIN) {
	            return true;
	        }
	    }
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
	    File configFile = new File(plugin.getDataFolder(), "config.yml");
	    if (!configFile.exists()) {
	        return false; // Si el archivo no existe, la función está deshabilitada por defecto.
	    }

	    FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

	    // Obtener el valor de anticap_enabled de config.yml
	    if (config.contains("Other.anticap_enabled")) {
	        return config.getBoolean("Other.anticap_enabled");
	    }

	    return false; // En caso de que la propiedad no exista, asumimos que la función está deshabilitada.
	}
	
	private boolean isPlayerIgnoring(Player sender, Set<Player> set) {
	    UUID senderUUID = sender.getUniqueId();

	    for (Player recipient : set) {
	        // Verificar si el destinatario está en la lista de ignorados del remitente
	        if (isPlayerIgnored(senderUUID, recipient.getUniqueId())) {
	            return true;
	        }
	    }

	    return false;
	}
	
	private void loadHoverGroupsFromConfig() {
        // Cargar grupos de hover desde el archivo de configuración
        hoverGroups = new HashMap<>();
        FileConfiguration config = plugin.getConfig();

        for (String groupName : config.getConfigurationSection("hover").getKeys(false)) {
            ConfigurationSection groupConfig = config.getConfigurationSection("hover." + groupName);

            boolean enabled = groupConfig.getBoolean("enabled");
            List<String> hoverText = groupConfig.getStringList("hoverText");
            String suggestCommand = groupConfig.getString("suggestCommand");

            HoverGroup hoverGroup = new HoverGroup(enabled, hoverText, suggestCommand);
            hoverGroups.put(groupName, hoverGroup);
        }
    }
	
	private boolean isPlayerIgnored(UUID senderUUID, UUID recipientUUID) {
	    File configFile = new File(plugin.getDataFolder(), "saves.yml");
	    FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

	    if (config.contains("ignore." + senderUUID.toString())) {
	        List<String> ignoredPlayers = config.getStringList("ignore." + senderUUID.toString());

	        // Verificar si el destinatario está en la lista de ignorados del remitente
	        return ignoredPlayers.contains(recipientUUID.toString());
	    }

	    return false;
	}
	
	private String getItemText(ItemStack itemStack) {
        if (itemStack != null && itemStack.getType() != Material.AIR) {
            String itemName = ChatColor.AQUA + itemStack.getType().toString();
            String itemDetails = ChatColor.GRAY + " (x" + itemStack.getAmount() + ")" + ChatColor.WHITE;
            return itemName + itemDetails;
        } else {
            // Devolver un mensaje indicando que no hay ningún ítem en la mano
            return ChatColor.RED + "No item" + ChatColor.WHITE;
        }
    }
	
	private void registerListeners() {
        plugin.getServer().getPluginManager().registerEvents(perWorldChat, plugin);
    }
	
	private String loadStaffChatFormatFromConfig() {
	    File configFile = new File(plugin.getDataFolder(), "config.yml");
	    FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

	    // Assuming the staffChatFormat is under the "Other" section of the config
	    if (config.contains("Staff.format")) {
	        return config.getString("Staff.format");
	    } else {
	        // Default format in case it's not specified in the config
	        return "&d[SC] &a%player% &e>> &f%message%";
	    }
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