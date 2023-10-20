package minealex.tchat.bot;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import minealex.tchat.TChat;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChatGames {
    private TChat plugin;
    private JSONObject currentGame;
    @SuppressWarnings("unused")
	private int taskId;
    private boolean hasSentMessage = false;
    private BukkitRunnable gameTimerTask;
    @SuppressWarnings("unused")
	private boolean isGameRunning;
    private boolean isGameActive = false;
    private BukkitTask activeGameTask;
    private boolean isGameInProgress = false;

    public ChatGames(TChat plugin) {
    	this.plugin = plugin;
        this.isGameRunning = false;
        this.currentGame = getRandomGame();
        startGameTimer();
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
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        long time = (Long) currentGame.get("time");
        int delay = (int) (time * 20);

        if (activeGameTask != null) {
            activeGameTask.cancel(); // Cancelar el temporizador del juego activo
        }

        activeGameTask = new BukkitRunnable() {
            @Override
            public void run() {
                currentGame = getRandomGame();
                if (currentGame != null) {
                    isGameActive = false;

                    // Reset flags
                    hasSentMessage = false;

                    // Broadcast the new game message here
                    String newGameMessage = ChatColor.translateAlternateColorCodes('&', (String) currentGame.get("message"));
                    Bukkit.broadcastMessage(newGameMessage);
                }
            }
        }.runTaskLater(plugin, delay);
    }

    public void cancelGameTimer() {
        if (gameTimerTask != null) {
            gameTimerTask.cancel();
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

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formattedReward);
                    });
                }

                JSONObject formatConfig = loadFormatConfig();
                String correctAnswerMessage = "&5TChat &e> &aCorrect answer! You receive a reward.";

                if (formatConfig != null && formatConfig.containsKey("correct_answer_message")) {
                    correctAnswerMessage = (String) formatConfig.get("correct_answer_message");
                }

                player.sendMessage(ChatColor.translateAlternateColorCodes('&', correctAnswerMessage));
                hasSentMessage = true;
                isGameActive = false;
                isGameInProgress = false;

                // Cancelar el juego actual
                cancelGameTimer();

                // Reset flags
                hasSentMessage = false;

                // Iniciar un nuevo juego
                currentGame = getRandomGame();
                if (currentGame != null) {
                    startGameTimer();
                }

                // Asegurarse de no procesar esta respuesta nuevamente
                return;
            }
        } else {
            hasSentMessage = false;
        }
    }
    
    private JSONObject loadFormatConfig() {
        File configFile = new File(plugin.getDataFolder(), "format_config.json");

        if (!configFile.exists()) {
            plugin.saveResource("format_config.json", false);
        } else {
            try (FileReader reader = new FileReader(configFile)) {
                JSONParser parser = new JSONParser();
                Object parsedObject = parser.parse(reader);
                if (parsedObject instanceof JSONObject) {
                    return (JSONObject) parsedObject;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
