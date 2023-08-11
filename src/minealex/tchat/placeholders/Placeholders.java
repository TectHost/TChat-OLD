package minealex.tchat.placeholders;

import java.util.Map.Entry;

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
	    }

	    return null;
	}

}
