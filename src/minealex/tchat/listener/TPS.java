package minealex.tchat.listener;

public class TPS {

    private static final int SAMPLE_INTERVAL = 20; // Intervalo de muestreo en ticks
    private static long[] tickTimes = new long[600]; // Almacena los tiempos de los últimos 600 ticks

    public static void recordTickTime(long tickTime) {
        System.arraycopy(tickTimes, 0, tickTimes, 1, tickTimes.length - 1);
        tickTimes[0] = tickTime;
    }

    public static double getTPS() {
        long elapsed = tickTimes[0] - tickTimes[tickTimes.length - 1];
        double ticksPerSecond = (double) (tickTimes.length - 1) / elapsed * SAMPLE_INTERVAL;
        return Math.min(ticksPerSecond, 20.0);
    }

    public static long[] getTickTimes() {
        return tickTimes.clone();
    }
}