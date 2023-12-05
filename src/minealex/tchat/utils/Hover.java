package minealex.tchat.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import minealex.tchat.TChat;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Hover implements Listener {

    private final TChat plugin;
    private String playerNameFormat;
    private List<String> hoverText;
    private Set<Player> processedPlayers;  // Para evitar el procesamiento duplicado

    public Hover(TChat plugin) {
        this.plugin = plugin;
        this.processedPlayers = new HashSet<>();
        loadConfig();
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        // Evitar el procesamiento duplicado
        if (processedPlayers.contains(player)) {
            return;
        }

        if (playerNameFormat != null && hoverText != null && !hoverText.isEmpty()) {
            playerNameFormat = PlaceholderAPI.setPlaceholders(player, playerNameFormat);

            String formattedName = ChatColor.translateAlternateColorCodes('&', playerNameFormat);
            TextComponent playerName = new TextComponent(formattedName);

            TextComponent message = new TextComponent(event.getMessage());
            message.setColor(ChatColor.WHITE);

            playerName.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + player.getName()));

            String translatedHoverText = PlaceholderAPI.setPlaceholders(player, String.join("\n", hoverText));
            playerName.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(translateColorCodes(translatedHoverText))
                            .create()));

            TextComponent finalMessage = new TextComponent(playerName, message);

            // Envía el mensaje personalizado al jugador
            player.spigot().sendMessage(finalMessage);

            // Envía el mensaje a la consola
            Bukkit.getServer().getConsoleSender().sendMessage(finalMessage.toLegacyText());

            // Evita que el código se ejecute dos veces para el mismo evento
            processedPlayers.add(player);
            event.setCancelled(true);  // Cancela el evento para evitar que se procese nuevamente
        }
    }

    private String translateColorCodes(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    private void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "format_config.json");
        if (!configFile.exists()) {
            plugin.getLogger().warning("El archivo format_config.json no existe.");
            return;
        }

        try (FileReader reader = new FileReader(configFile)) {
            JsonParser parser = new JsonParser();
            JsonObject jsonObject = parser.parse(reader).getAsJsonObject();

            playerNameFormat = jsonObject.get("format").getAsString();

            hoverText = new ArrayList<>();
            if (jsonObject.has("Hover")) {
                JsonArray hoverArray = jsonObject.getAsJsonArray("Hover");
                for (JsonElement element : hoverArray) {
                    hoverText.add(element.getAsString());
                }
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Error al leer format_config.json: " + e.getMessage());
        } catch (Exception e) {
            plugin.getLogger().warning("Error al leer la configuración desde format_config.json: " + e.getMessage());
        }
    }
}
