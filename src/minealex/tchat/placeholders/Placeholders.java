package minealex.tchat.placeholders;

import java.util.List;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import minealex.tchat.TChat;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;

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

            // Si no se encontró un grupo, usar el sufijo predeterminado
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
        } else if (identifier.equals("nickname")) {
            return obtenerNick(player);
        } else if (identifier.equals("ignored")) {
        return obtenerJugadoresIgnorados(player);
        } else if (identifier.equals("luckperms_prefix")) {
            return obtenerLuckPermsPrefix(player);
        } else if (identifier.equals("luckperms_suffix")) {
            return obtenerLuckPermsSuffix(player);
        } else if (identifier.equals("group")) {
            return obtenerNombreGrupo(player);
        } else if (identifier.equals("chatgames_wins")) {
            return String.valueOf(obtenerChatGamesWins(player));
        }
        

        return null;
    }

    private String obtenerColorAsignado(Player player) {
        String playerName = player.getName();
        String colorName = plugin.getConfig().getString("players." + player.getUniqueId() + ".chatcolor");
        String format = plugin.getConfig().getString("players." + player.getUniqueId() + ".format"); // Obtener el formato

        if (colorName != null && format != null) {
            return ChatColor.valueOf(colorName) + ChatColor.translateAlternateColorCodes('&', format); // Aplicar color y formato
        }

        // Si el jugador no tiene un color o formato asignado, devolvemos un color predeterminado
        return ChatColor.RESET.toString(); // Puedes cambiar esto al color que prefieras.
    }
    
    private String obtenerLuckPermsPrefix(Player player) {
        User user = LuckPermsProvider.get().getUserManager().getUser(player.getUniqueId());

        if (user != null) {
            String prefix = user.getCachedData().getMetaData().getPrefix();
            return prefix != null ? prefix : "";
        }

        return "";
    }
    
    private int obtenerChatGamesWins(Player player) {
        String uuid = player.getUniqueId().toString();
        int wins = plugin.getConfig().getInt("players." + uuid + ".chatgames_wins");
        return wins;
    }
    
    private String obtenerNombreGrupo(Player player) {
        // Verificar si el jugador tiene un grupo asignado
        for (Entry<String, minealex.tchat.TChat.ChatGroup> entry : plugin.getGroups().entrySet()) {
            String groupName = entry.getKey();

            if (player.hasPermission("tchat.group." + groupName)) {
                return groupName;
            }
        }

        // Si no se encontró un grupo, devolver un valor predeterminado o vacío según lo desees
        return "";
    }

    private String obtenerLuckPermsSuffix(Player player) {
        User user = LuckPermsProvider.get().getUserManager().getUser(player.getUniqueId());

        if (user != null) {
            String suffix = user.getCachedData().getMetaData().getSuffix();
            return suffix != null ? suffix : "";
        }

        return "";
    }
    
    private String obtenerJugadoresIgnorados(Player player) {
        String uuid = player.getUniqueId().toString();
        List<String> jugadoresIgnorados = plugin.getConfig().getStringList("players." + uuid + ".ignore");

        if (jugadoresIgnorados != null) {
            return String.valueOf(jugadoresIgnorados.size());
        }

        return "0";
    }

    private String obtenerNick(Player player) {
        String playerName = player.getName();
        String nick = plugin.getConfig().getString("players." + player.getUniqueId() + ".nick");

        if (nick != null) {
            return nick;
        }

        return playerName;
    }

    private boolean isValidColorCode(String colorCode) {
        // Verifica si el código de color es válido (por ejemplo, &a, &b, etc.)
        return colorCode.matches("&[0-9a-fA-F]");
    }
}
