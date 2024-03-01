package minealex.tchat.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Map;

public class InfoCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public InfoCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("unchecked")
	@Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            File configFile = new File(plugin.getDataFolder(), "messages.yml");

            if (configFile.exists()) {
                Yaml yaml = new Yaml();
                Map<String, Object> config = (Map<String, Object>) yaml.load(new FileReader(configFile));

                List<String> infoArray = (List<String>) config.get("Info");
                Map<String, String> messages = (Map<String, String>) config.get("messages");
                String noPermissionMessage = messages.get("noPermission");

                // Verificar permisos
                if (!sender.hasPermission("tchat.info")) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', noPermissionMessage));
                    return true;
                }

                // Resto del código para enviar la información
                for (String infoMessage : infoArray) {
                    // Agregar información adicional
                    int port = Bukkit.getServer().getPort();
                    long totalMem = Runtime.getRuntime().totalMemory() / 1024L / 1024L;
                    String version = Bukkit.getServer().getVersion();
                    long freeMem = Runtime.getRuntime().freeMemory() / 1024L / 1024L;
                    String javaVersion = System.getProperty("java.version");
                    double tps = 20.0;  // Assuming TPS is 20 by default
                    double cpu = (double) Runtime.getRuntime().availableProcessors();
                    String osN = System.getProperty("os.name");
                    String osV = System.getProperty("os.version");
                    long maxMem = Runtime.getRuntime().maxMemory() / 1024L / 1024L;
                    long usedMem = totalMem - freeMem;
                    String cpuFamily = System.getenv("PROCESSOR_IDENTIFIER");
                    int numPlugins = Bukkit.getServer().getPluginManager().getPlugins().length;
                    int numWorlds = Bukkit.getServer().getWorlds().size();

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
                    infoMessage = infoMessage.replace("%plugins%", String.valueOf(numPlugins));
                    infoMessage = infoMessage.replace("%worlds%", String.valueOf(numWorlds));

                    // Enviar el mensaje con información adicional
                    sender.sendMessage(infoMessage);
                }

            } else {
                sender.sendMessage("File 'messages.yml' was not found.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }
}
