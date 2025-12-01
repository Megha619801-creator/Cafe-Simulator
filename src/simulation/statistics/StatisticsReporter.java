package simulation.statistics;

import java.util.Locale;

/**
 * Helper for printing derived statistics to the console.
 */
public final class StatisticsReporter {

    private StatisticsReporter() {
    }

    public static void printToConsole(SimulationStatistics statistics) {
        System.out.println("=== System-wide metrics ===");
        printLine("Utilization U = B / T", statistics.getSystemUtilization());
        printLine("Throughput X = C / T", statistics.getThroughput());
        printLine("Average service S = B / C", statistics.getAverageServiceTime());
        printLine("Average response R = W / C", statistics.getAverageResponseTime());
        printLine("Average waiting W / C", statistics.getAverageWaitingTime());
        printLine("Average number N = W / T", statistics.getAverageNumberInSystem());

        System.out.println("\n=== Service point metrics ===");
        System.out.printf("%-20s %10s %10s %12s %12s %12s%n",
                "Name", "Arrivals", "Done", "U=B/T", "X=C/T", "S=B/C");
        for (ServicePointStatistics stat : statistics.getServicePointStatistics()) {
            System.out.printf(Locale.US, "%-20s %10d %10d %12.3f %12.3f %12.3f%n",
                    stat.getServicePointName(),
                    stat.getArrivals(),
                    stat.getCompletions(),
                    stat.getUtilization(statistics.getSimulationTime()),
                    stat.getThroughput(statistics.getSimulationTime()),
                    stat.getAverageServiceTime());
        }
        System.out.println();
    }

    private static void printLine(String label, double value) {
        System.out.printf(Locale.US, "%-30s : %.3f%n", label, value);
    }
}
