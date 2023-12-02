package minealex.tchat.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.clip.placeholderapi.PlaceholderAPI;
import minealex.tchat.TChat;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Hover implements Listener {

    private final TChat plugin;
    private String playerNameFormat;
    private List<String> hoverText;  // Variable para almacenar el texto de Hover

    public Hover(TChat plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (playerNameFormat != null && hoverText != null && !hoverText.isEmpty()) {
            playerNameFormat = PlaceholderAPI.setPlaceholders(player, playerNameFormat);

            String formattedName = ChatColor.translateAlternateColorCodes('&', playerNameFormat);
            TextComponent playerName = new TextComponent(formattedName);

            TextComponent message = new TextComponent(event.getMessage());
            message.setColor(ChatColor.WHITE);

            playerName.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + player.getName()));
            
            String translatedHoverText = PlaceholderAPI.setPlaceholders(player, String.join("\n", hoverText));

            // Utiliza el texto de Hover obtenido del archivo de configuración
            playerName.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(translateColorCodes(translatedHoverText))
                            .create()));

            TextComponent finalMessage = new TextComponent(playerName, message);

            event.setCancelled(true);
            player.spigot().sendMessage(finalMessage);
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

            // Obtén el texto de Hover desde la sección "Hover" del JSON
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
