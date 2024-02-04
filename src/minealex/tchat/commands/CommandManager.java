package minealex.tchat.commands;

import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.clip.placeholderapi.PlaceholderAPI;
import minealex.tchat.TChat;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class CommandManager implements Listener {
    private final TChat plugin;
    private Map<UUID, Long> commandCooldowns = new HashMap<>();
    private PlayerCommandPreprocessEvent lastCommandEvent;
    
    public CommandManager(TChat plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void CustomCommand(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();
        String message = e.getMessage();
        lastCommandEvent = e;

        if (message.startsWith("/")) {
            String[] commandArgs = message.substring(1).split(" ");
            String commandName = commandArgs[0].toLowerCase(); // Obtén el nombre del comando

            if (plugin.getConfigManager().getCommands().contains("commands." + commandName)) {
                List<String> commandConfig = plugin.getConfigManager().getCommands().getStringList("commands." + commandName);
                boolean allowArgs = plugin.getConfigManager().getCommands().getBoolean("custom-commands." + commandName + ".args");

                if (allowArgs) {
                    // Verifica si se proporcionaron suficientes argumentos
                    if (commandArgs.length > 1) {
                        String args = message.substring(commandArgs[0].length() + 1); // Incluye el espacio después del nombre del comando
                        commandConfig = commandConfig.stream().map(action -> action.replace("{args}", args)).collect(Collectors.toList());
                        plugin.getLogger().info("Arguments provided: " + args);
                    } else {
                        player.sendMessage(ChatColor.RED + "Formato incorrecto. Uso: /" + commandName + " <argumentos>");
                        return;
                    }
                }
                
                if (commandConfig.contains("permission-required")) {
                    String permission = "tchat.customcommand." + commandName;
                    if (!player.hasPermission(permission)) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessagesYML("messages.noPermission")));
                        return;
                    }
                }

                int cooldownSeconds = getCooldown(commandName);
                if (cooldownSeconds > 0) {
                    if (isOnCooldown(player, commandName, cooldownSeconds)) {
                        player.sendMessage("Wait " + getRemainingCooldown(player, commandName) + " seconds before using this command again.");
                        return;
                    }
                }

                List<String> actions = plugin.getConfigManager().getCommands().getStringList("commands." + commandName + ".actions");
                for (String action : actions) {
                    executeAction(player, action, commandArgs);
                }

                if (cooldownSeconds > 0) {
                    setCooldown(player, commandName, cooldownSeconds);
                    plugin.getLogger().info("Arguments for actions: " + commandArgs);
                }
                
                e.setCancelled(true);
            }
        }
    }

    @SuppressWarnings("deprecation")
	private void executeAction(Player player, String action, String[] args) {
    	String message = lastCommandEvent.getMessage();
        switch (action.split(" ")[0]) {
            case "[MESSAGE]":
            	String rawMessage = action.replace("[MESSAGE] ", "");
            	String formattedMessage = PlaceholderAPI.setPlaceholders(player, rawMessage);
            	formattedMessage = ChatColor.translateAlternateColorCodes('&', formattedMessage);
                player.sendMessage(formattedMessage);
                break;
            case "[TITLE]":
                String titleData = action.replace("[TITLE] ", "");
                String[] titleParts = titleData.split(";");
                if (titleParts.length == 2) {
                    String title = ChatColor.translateAlternateColorCodes('&', titleParts[0]);
                    String subtitle = ChatColor.translateAlternateColorCodes('&', titleParts[1]);

                    player.sendTitle(title, subtitle);
                }
                break;
            case "[PLAYER_COMMAND]":
                String playerCommand = action.replace("[PLAYER_COMMAND] ", "");
                String finalPlayerCommand;

                if (args.length > 1) {
                    String args1 = message.substring(args[0].length() + 1);
                    finalPlayerCommand = playerCommand.replace("{player}", player.getName()) + " " + args1;
                    plugin.getLogger().info("Arguments for [PLAYER_COMMAND]: " + args1);
                } else {
                    finalPlayerCommand = playerCommand.replace("{player}", player.getName());
                }

                player.performCommand(finalPlayerCommand);
                plugin.getLogger().info("Executing [PLAYER_COMMAND] action with command: " + finalPlayerCommand);
                break;
            case "[CONSOLE_COMMAND]":
                String consoleCommand = action.replace("[CONSOLE_COMMAND] ", "");
                consoleCommand = consoleCommand.replace("{player}", player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), consoleCommand);
                break;
            case "[SOUND]":
                String sound = action.replace("[SOUND] ", "");
                player.playSound(player.getLocation(), Sound.valueOf(sound), 1, 1);
                break;
            case "[PARTICLE]":
                String particleName = action.replace("[PARTICLE] ", "");

                try {
                    System.out.println("Attempting to use particle effect: " + particleName);
                    Effect particleEffect = Effect.valueOf(particleName);
                    player.playEffect(player.getLocation(), particleEffect, 1);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
                break;
            case "[TELEPORT]":
                String teleportData = action.replace("[TELEPORT] ", "");
                String[] teleportParts = teleportData.split(";");
                
                if (teleportParts.length == 4) {
                    String worldName = teleportParts[0];
                    double x = Double.parseDouble(teleportParts[1]);
                    double y = Double.parseDouble(teleportParts[2]);
                    double z = Double.parseDouble(teleportParts[3]);

                    World world = Bukkit.getWorld(worldName);
                    
                    if (world != null) {
                        Location teleportLocation = new Location(world, x, y, z);
                        player.teleport(teleportLocation);
                    }
                }
                break;
            case "[POTION_EFFECT]":
                String effectData = action.replace("[POTION_EFFECT] ", "");
                String[] effectParts = effectData.split(";");

                if (effectParts.length == 3) {
                    String effectType = effectParts[0];
                    int duration = Integer.parseInt(effectParts[1]);
                    int amplifier = Integer.parseInt(effectParts[2]);

                    PotionEffectType potionEffectType = PotionEffectType.getByName(effectType.toUpperCase());
                    if (potionEffectType != null) {
                        PotionEffect potionEffect = new PotionEffect(potionEffectType, duration, amplifier);
                        player.addPotionEffect(potionEffect);
                    }
                }
                break;
            case "[ACTION_BAR]":
                String actionBarMessage = action.replace("[ACTION_BAR] ", "");
                String processedMessage = PlaceholderAPI.setPlaceholders(player, actionBarMessage);

                sendActionBar(player, processedMessage);
                break;
            case "[INVENTORY]":
                String inventoryAction = action.replace("[INVENTORY] ", "");
                String[] parts = inventoryAction.split(" ");

                if (parts.length >= 3) {
                    String actionType = parts[0];
                    String itemName = parts[1];
                    String quantityStr = parts[2];

                    int quantity = 1;

                    try {
                        quantity = Integer.parseInt(quantityStr);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }

                    switch (actionType) {
                        case "ADD":
                            player.getInventory().addItem(new ItemStack(Material.valueOf(itemName), quantity));
                            break;
                        case "REMOVE":
                            player.getInventory().removeItem(new ItemStack(Material.valueOf(itemName), quantity));
                            break;
                        case "CHANGE":
                            String newItemName = parts[3];
                            Material newMaterial = Material.matchMaterial(newItemName);

                            if (newMaterial != null) {
                                ItemStack newItemStack = new ItemStack(newMaterial, quantity);

                                // Verificar si el jugador tiene la espada de diamante antes de realizar el cambio
                                ItemStack diamondSword = new ItemStack(Material.DIAMOND_SWORD);
                                if (player.getInventory().containsAtLeast(diamondSword, quantity)) {
                                    // Quitar la espada de diamante del inventario
                                    player.getInventory().removeItem(diamondSword);

                                    // Agregar la nueva manzana al inventario
                                    player.getInventory().addItem(newItemStack);
                                } else {
                                    // Mensaje si el jugador no tiene suficientes espadas de diamante
                                    player.sendMessage(ChatColor.RED + "No tienes suficientes materiales para realizar el cambio.");
                                }
                            } else {
                                // Mensaje si el nuevo material no es válido
                                player.sendMessage(ChatColor.RED + "Material inválido para el cambio.");
                            }
                            break;
                        default:
                            // Handle unknown action type
                            player.sendMessage(ChatColor.RED + "Unknown inventory action type.");
                            break;
                    }
                } else {
                    // Handle case when the command is not properly formatted
                    player.sendMessage(ChatColor.RED + "Invalid format for inventory action.");
                }
                break;
        }
    }

    private int getCooldown(String commandName) {
        Object commandConfigObject = plugin.getConfigManager().getCommands();
        
        if (commandConfigObject instanceof YamlConfiguration) {
            YamlConfiguration commandConfig = (YamlConfiguration) commandConfigObject;
            
            if (commandConfig.contains("commands." + commandName + ".cooldown")) {
                return commandConfig.getInt("commands." + commandName + ".cooldown");
            }
        }
        return 0;
    }

    private boolean isOnCooldown(Player player, String commandName, int cooldownSeconds) {
        long currentTime = System.currentTimeMillis() / 1000;
        long lastExecution = commandCooldowns.getOrDefault(player.getUniqueId(), 0L);
        long cooldownEnd = lastExecution + cooldownSeconds;

        return currentTime < cooldownEnd;
    }

    private int getRemainingCooldown(Player player, String commandName) {
        long currentTime = System.currentTimeMillis() / 1000;
        long lastExecution = commandCooldowns.getOrDefault(player.getUniqueId(), 0L);
        int cooldownSeconds = getCooldown(commandName);
        long cooldownEnd = lastExecution + cooldownSeconds;

        return (int) (cooldownEnd - currentTime);
    }
    
    private void sendActionBar(Player player, String message) {
        try {
            Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer");
            Object craftPlayer = craftPlayerClass.cast(player);

            Class<?> packetPlayOutChatClass = Class.forName("net.minecraft.server.v1_8_R3.PacketPlayOutChat");
            Class<?> iChatBaseComponentClass = Class.forName("net.minecraft.server.v1_8_R3.IChatBaseComponent");

            Object chatComponentText = iChatBaseComponentClass.getDeclaredClasses()[0]
                    .getMethod("a", String.class)
                    .invoke(null, "{\"text\":\"" + ChatColor.translateAlternateColorCodes('&', message) + "\"}");

            Object packetPlayOutChat = packetPlayOutChatClass
                    .getConstructor(iChatBaseComponentClass, byte.class)
                    .newInstance(chatComponentText, (byte) 2);

            // Get the player's handle and player connection
            Object craftPlayerHandle = craftPlayerClass.getMethod("getHandle").invoke(craftPlayer);
            Object playerConnection = craftPlayerHandle.getClass().getField("playerConnection").get(craftPlayerHandle);

            // Find the sendPacket method with the correct parameter types
            for (Method method : playerConnection.getClass().getMethods()) {
                if (method.getName().equals("sendPacket")) {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length == 1 && parameterTypes[0].isAssignableFrom(packetPlayOutChatClass)) {
                        method.invoke(playerConnection, packetPlayOutChat);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setCooldown(Player player, String commandName, int cooldownSeconds) {
        long currentTime = System.currentTimeMillis() / 1000;
        commandCooldowns.put(player.getUniqueId(), currentTime);
    }
}