package minealex.tchat.placeholders;

import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import minealex.tchat.ChatFormatConfig;
import minealex.tchat.ChatGroup;
import minealex.tchat.TChat;

@SuppressWarnings("unused")
public class Placeholders extends PlaceholderExpansion {

	private TChat plugin;

	public Placeholders(TChat plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean persist() {
		return true;
	}

	@Override
	public boolean canRegister() {
		return true;
	}

	@Override
	public String getAuthor() {
		return "Mine_Alex";
	}

	@Override
	public String getIdentifier() {
		return "tchat";
	}

	@Override
	public String getVersion() {
		return plugin.getDescription().getVersion();
	}

	@Override
	public String onPlaceholderRequest(Player player, String identifier) {
	    if (player == null) {
	        return "";
	    }

	    if (identifier.equals("suffix")) {
	        minealex.tchat.TChat.ChatGroup chatGroup = null;

	        // Verificar si el jugador tiene un grupo asignado
	        for (Entry<String, minealex.tchat.TChat.ChatGroup> entry : plugin.getGroups().entrySet()) {
	            String groupName = entry.getKey();

	            if (player.hasPermission("tchat.group." + groupName)) {
	                chatGroup = entry.getValue();
	                break;
	            }
	        }

	        // Si no se encontró un grupo, usar el prefijo predeterminado
	        if (chatGroup == null) {
	            chatGroup = plugin.getDefaultChatGroup();
	        }

	        if (chatGroup == null) {
	            return "";
	        } else {
	            return chatGroup.getSuffix();
	        }
	    } else if (identifier.equals("prefix")) {
	        minealex.tchat.TChat.ChatGroup chatGroup = null;

	        // Verificar si el jugador tiene un grupo asignado
	        for (Entry<String, minealex.tchat.TChat.ChatGroup> entry : plugin.getGroups().entrySet()) {
	            String groupName = entry.getKey();

	            if (player.hasPermission("tchat.group." + groupName)) {
	                chatGroup = entry.getValue();
	                break;
	            }
	        }

	        // Si no se encontró un grupo, usar el prefijo predeterminado
	        if (chatGroup == null) {
	            chatGroup = plugin.getDefaultChatGroup();
	        }

	        if (chatGroup == null) {
	            return "";
	        } else {
	            return chatGroup.getPrefix();
	        }
	    } else if (identifier.equals("chatcolor")) {
            return obtenerColorAsignado(player);
        }

	    return null;
	}
	
	private String obtenerColorAsignado(Player player) {
	    String playerName = player.getName();
	    String colorName = plugin.getConfig().getString("players." + player.getUniqueId() + ".chatcolor");

	    if (colorName != null) {
	        return ChatColor.valueOf(colorName).toString();
	    }

	    // Si el jugador no tiene un color asignado, devolvemos un color predeterminado
	    return ChatColor.RESET.toString(); // Puedes cambiar esto al color que prefieras.
	}

	private boolean isValidColorCode(String colorCode) {
	    // Verifica si el código de color es válido (por ejemplo, &a, &b, etc.)
	    return colorCode.matches("&[0-9a-fA-F]");
	}
}
