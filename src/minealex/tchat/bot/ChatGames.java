package minealex.tchat.bot;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import me.clip.placeholderapi.PlaceholderAPI;
import minealex.tchat.TChat;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class ChatGames {
    private TChat plugin;
    private JSONObject currentGame;
    private boolean hasSentMessage = false;
    private BukkitTask activeGameTask;
    private boolean isGameActive = false;
    private boolean isGameInProgress = false;
    private String gameTitle;
    private String gameSubtitle;
    private String sound;

    public ChatGames(TChat plugin) {
        this.plugin = plugin;
        this.currentGame = getRandomGame();

        if (this.currentGame != null) {
            this.gameTitle = ChatColor.translateAlternateColorCodes('&', (String) this.currentGame.get("title"));
            this.gameSubtitle = ChatColor.translateAlternateColorCodes('&', (String) this.currentGame.get("subtitle"));
            this.sound = (String) this.currentGame.get("sound");
            startGameTimer();
        } else {
            Bukkit.getLogger().warning("No enabled games found in the configuration.");
        }
    }

    @SuppressWarnings("unchecked")
	private JSONArray loadChatGamesConfig() {
        File configFile = new File(plugin.getDataFolder(), "chatgames.json");

        if (!configFile.exists()) {
            plugin.saveResource("chatgames.json", false);
        } else {
            try (FileReader reader = new FileReader(configFile)) {
                JSONParser parser = new JSONParser();
                Object parsedObject = parser.parse(reader);
                if (parsedObject instanceof JSONArray) {
                    return (JSONArray) parsedObject;
                } else if (parsedObject instanceof JSONObject) {
                    JSONArray jsonArray = new JSONArray();
                    jsonArray.add(parsedObject);
                    return jsonArray;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void incrementarChatGamesWins(Player player) {
        String uuid = player.getUniqueId().toString();
        File savesFile = new File(plugin.getDataFolder(), "saves.yml");
        YamlConfiguration savesConfig = YamlConfiguration.loadConfiguration(savesFile);
        int currentWins = savesConfig.getInt("players." + uuid + ".chatgames_wins", 0);
        int newWins = currentWins + 1;
        savesConfig.set("players." + uuid + ".chatgames_wins", newWins);

        try {
            savesConfig.save(savesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JSONObject getRandomGame() {
        JSONArray games = loadChatGamesConfig();
        if (games != null && !games.isEmpty()) {
            List<JSONObject> enabledGames = new ArrayList<>();

            for (Object obj : games) {
                JSONObject game = (JSONObject) obj;
                boolean isEnabled = (boolean) game.get("enabled");

                if (isEnabled) {
                    enabledGames.add(game);
                }
            }

            if (!enabledGames.isEmpty()) {
                Random random = new Random();
                int index = random.nextInt(enabledGames.size());
                return enabledGames.get(index);
            }
        }
        return null;
    }

    @SuppressWarnings("unused")
	public void startGameTimer() {
        if (currentGame == null || isGameActive) {
            return;
        }

        isGameActive = true;
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        long time = (Long) currentGame.get("time");
        int delay = (int) (time * 20);

        if (activeGameTask != null) {
            activeGameTask.cancel();
        }

        activeGameTask = new BukkitRunnable() {
            @Override
            public void run() {
                currentGame = getRandomGame();
                if (currentGame != null) {
                    isGameActive = false;
                    hasSentMessage = false;
                    String newGameMessage = ChatColor.translateAlternateColorCodes('&', (String) currentGame.get("message"));

                    // Mostrar el título solo si la opción title-enabled está habilitada
                    if ((boolean) currentGame.get("title-enabled")) {
                        broadcastTitle();
                    }

                    // Enviar el mensaje personalizado al jugador con hoverText
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        @SuppressWarnings("unchecked")
                        String hoverText = String.join("\n", (JSONArray) currentGame.get("hoverText"));
                        String translatedHoverText = PlaceholderAPI.setPlaceholders(player, hoverText);

                        // Obtener el comando personalizado
                        String clickCommand = (String) currentGame.get("click-command");

                        // Reemplazar %player% con el nombre del jugador
                        clickCommand = clickCommand.replace("%player%", player.getName());

                        // Crear el mensaje con hoverText y clickEvent
                        TextComponent finalMessage = createHoverTextMessage(newGameMessage, translatedHoverText, clickCommand);

                        // Envía el mensaje personalizado al jugador
                        player.spigot().sendMessage(finalMessage);
                    }

                    int responseTime = ((Long) currentGame.get("time")).intValue();
                    int responseDelay = responseTime * 20;

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (!hasSentMessage) {
                                isGameActive = false;
                                isGameInProgress = false;
                                currentGame = getRandomGame();
                                if (currentGame != null) {
                                    startGameTimer();
                                }
                            }
                        }
                    }.runTaskLater(plugin, responseDelay);
                }
            }
        }.runTaskLater(plugin, delay);
    }

    @SuppressWarnings("deprecation")
	private void broadcastTitle() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle(gameTitle, gameSubtitle);

            if ((boolean) currentGame.get("sound-enabled")) {
                playSound(player);
            }
        }
    }

    private void playSound(Player player) {
        // Verificar si el sonido está habilitado
        if ((boolean) currentGame.get("sound-enabled")) {
            player.playSound(player.getLocation(), Sound.valueOf(sound), 1.0F, 1.0F);
        }
    }

    public void processChat(Player player, String message) {
        if (currentGame == null || isGameActive || isGameInProgress) return;

        String keyword = (String) currentGame.get("keyword");

        if (message.equalsIgnoreCase(keyword)) {
            if (!hasSentMessage) {
                JSONArray rewards = (JSONArray) currentGame.get("rewards");
                for (Object rewardObj : rewards) {
                    String reward = (String) rewardObj;
                    String formattedReward = reward.replace("%winner%", player.getName());

                    if ((boolean) currentGame.get("firework-enabled")) {
                        showFirework(player);
                    }

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formattedReward);
                    });
                }

                incrementarChatGamesWins(player);
                
                String correctAnswerMessage = plugin.getMessagesYML("messages.correct_answer_message");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', correctAnswerMessage));
                
                hasSentMessage = true;
                isGameActive = false;
                isGameInProgress = false;

                cancelGameTimer();

                hasSentMessage = false;

                currentGame = getRandomGame();
                if (currentGame != null) {
                    startGameTimer();
                }

                return;
            }
        } else {
            hasSentMessage = false;
        }
    }
    
    public void cancelGameTimer() {
        if (activeGameTask != null) {
            activeGameTask.cancel();
        }
    }

    private void showFirework(Player player) {
        boolean fireworkEnabled = (boolean) currentGame.get("firework-enabled");
        String fireworkColor = (String) currentGame.get("firework-color");

        if (fireworkEnabled) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                Firework firework = (Firework) player.getWorld().spawn(player.getLocation(), Firework.class);
                FireworkMeta meta = firework.getFireworkMeta();
                FireworkEffect.Builder builder = FireworkEffect.builder();

                builder.withColor(Color.fromRGB(getRGBFromColorName(fireworkColor)));

                meta.addEffect(builder.build());
                firework.setFireworkMeta(meta);
            });
        }
    }

    private int getRGBFromColorName(String colorName) {
        switch (colorName.toUpperCase()) {
            case "AQUA":
                return 0x00FFFF;
            case "BLACK":
                return 0x000000;
            case "BLUE":
                return 0x0000FF;
            case "FUCHSIA":
                return 0xFF00FF;
            case "GRAY":
                return 0x808080;
            case "GREEN":
                return 0x008000;
            case "LIME":
                return 0x00FF00;
            case "MAROON":
                return 0x800000;
            case "NAVY":
                return 0x000080;
            case "OLIVE":
                return 0x808000;
            case "PURPLE":
                return 0x800080;
            case "RED":
                return 0xFF0000;
            case "SILVER":
                return 0xC0C0C0;
            case "TEAL":
                return 0x008080;
            case "WHITE":
                return 0xFFFFFF;
            case "YELLOW":
                return 0xFFFF00;
            default:
                return 0xFFFFFF;
        }
    }

    private TextComponent createHoverTextMessage(String mainMessage, String hoverText, String clickCommand) {
        TextComponent playerName = new TextComponent(mainMessage);
        
        playerName.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, clickCommand));

        playerName.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new BaseComponent[]{new TextComponent(ChatColor.translateAlternateColorCodes('&', hoverText))}));

        return playerName;
    }
}
