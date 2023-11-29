package minealex.tchat.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import minealex.tchat.listener.TPS;

import java.io.File;
import java.io.FileReader;

public class InfoCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public InfoCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	try {
            File configFile = new File(plugin.getDataFolder(), "format_config.json");

            if (configFile.exists()) {
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(configFile));

                JSONArray infoArray = (JSONArray) jsonObject.get("Info");
                JSONObject messagesObject = (JSONObject) jsonObject.get("messages");
                String noPermissionMessage = (String) messagesObject.get("noPermission");

                // Verificar permisos
                if (!sender.hasPermission("tchat.info")) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', noPermissionMessage));
                    return true;
                }

                // Resto del código para enviar la información
                for (Object info : infoArray) {
                    String infoMessage = (String) info;

                    // Agregar información adicional
                    int port = Bukkit.getServer().getPort();
                    long totalMem = Runtime.getRuntime().totalMemory() / 1024L / 1024L;
                    String version = Bukkit.getServer().getVersion();
                    long freeMem = Runtime.getRuntime().freeMemory() / 1024L / 1024L;
                    String javaVersion = System.getProperty("java.version");
                    double tps = TPS.getTPS();
                    double cpu = (double) Runtime.getRuntime().availableProcessors();
                    String osN = System.getProperty("os.name");
                    String osV = System.getProperty("os.version");
                    long maxMem = Runtime.getRuntime().maxMemory() / 1024L / 1024L;
                    long usedMem = totalMem - freeMem;
                    String cpuFamily = System.getenv("PROCESSOR_IDENTIFIER");

                    // Reemplazar placeholders en el mensaje
                    infoMessage = ChatColor.translateAlternateColorCodes('&', infoMessage);
                    infoMessage = infoMessage.replace("%java_version%", javaVersion);
                    infoMessage = infoMessage.replace("%used_memory%", String.valueOf(usedMem));
                    infoMessage = infoMessage.replace("%port%", String.valueOf(port));
                    infoMessage = infoMessage.replace("%version%", version);
                    infoMessage = infoMessage.replace("%free_memory%", String.valueOf(freeMem));
                    infoMessage = infoMessage.replace("%os_name%", osN);
                    infoMessage = infoMessage.replace("%os_version%", osV);
                    infoMessage = infoMessage.replace("%tps%", String.valueOf(tps));
                    infoMessage = infoMessage.replace("%cpu_cores%", String.valueOf(cpu));
                    infoMessage = infoMessage.replace("%max_memory%", String.valueOf(maxMem));
                    infoMessage = infoMessage.replace("%total_memory%", String.valueOf(totalMem));
                    infoMessage = infoMessage.replace("%cpu_family%", cpuFamily);

                    // Enviar el mensaje con información adicional
                    sender.sendMessage(infoMessage);
                }

            } else {
                sender.sendMessage("File 'format_config.json' was not found.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }
}
