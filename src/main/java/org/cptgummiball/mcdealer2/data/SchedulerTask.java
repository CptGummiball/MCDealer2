package org.cptgummiball.mcdealer2.data;

import org.bukkit.scheduler.BukkitRunnable;
import org.cptgummiball.mcdealer2.MCDealer2;

public class SchedulerTask {

    private final MCDealer2 plugin;
    private final int interval; // Intervall in Sekunden

    public SchedulerTask(MCDealer2 plugin, int interval) {
        this.plugin = plugin;
        this.interval = interval;
    }

    public void start() {
        // Scheduler für wiederholte Ausführung
        new BukkitRunnable() {
            @Override
            public void run() {
                // ShopDataProvider main-Methode aufrufen
                ShopDataProvider.generateOutputJson();
            }
        }.runTaskTimer(plugin, 0, interval * 20L); // 20L entspricht 1 Tick (1/20 Sekunde)
    }

    // Hilfsmethode, um die Zeitangabe (z.B. "1m", "30s") in Sekunden zu konvertieren
    public static int parseInterval(String intervalString) {
        int interval = 0;
        char unit = intervalString.charAt(intervalString.length() - 1);

        try {
            int value = Integer.parseInt(intervalString.substring(0, intervalString.length() - 1));
            switch (unit) {
                case 's': // Sekunden
                    interval = value;
                    break;
                case 'm': // Minuten
                    interval = value * 60;
                    break;
                case 'h': // Stunden
                    interval = value * 3600;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid interval unit");
            }
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Invalid interval format");
        }
        return interval;
    }
}

