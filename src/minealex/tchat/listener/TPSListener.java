package minealex.tchat.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class TPSListener implements Listener {

    private final Plugin plugin;

    public TPSListener(Plugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        startTPSTask();
    }

    private void startTPSTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long[] tickTimes = TPS.getTickTimes();
            for (long tickTime : tickTimes) {
                TPS.recordTickTime(tickTime);
            }
        }, 0L, 1L);
    }
}
