package simulation.data;

import simulation.config.SimulationParameters;
import simulation.statistics.ServicePointStatistics;
import simulation.statistics.SimulationStatistics;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;

/**
 * Loads simulation parameters from a properties file.
 */
public class FileManager {

    private FileManager() {
    }

    public static SimulationParameters loadParameters(Path path) throws IOException {
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            props.load(in);
        }
        SimulationParameters.Builder builder = SimulationParameters.builder();
        if (props.containsKey("instore.arrival.mean")) {
            builder.instoreArrivalMean(Double.parseDouble(props.getProperty("instore.arrival.mean")));
        }
        if (props.containsKey("mobile.arrival.mean")) {
            builder.mobileArrivalMean(Double.parseDouble(props.getProperty("mobile.arrival.mean")));
        }
        if (props.containsKey("cashier.service.mean")) {
            builder.cashierServiceMean(Double.parseDouble(props.getProperty("cashier.service.mean")));
        }
        if (props.containsKey("barista.service.mean")) {
            builder.baristaServiceMean(Double.parseDouble(props.getProperty("barista.service.mean")));
        }
        if (props.containsKey("barista.service.variance")) {
            builder.baristaServiceVariance(Double.parseDouble(props.getProperty("barista.service.variance")));
        }
        if (props.containsKey("shelf.service.min")) {
            builder.shelfServiceMin(Double.parseDouble(props.getProperty("shelf.service.min")));
        }
        if (props.containsKey("shelf.service.max")) {
            builder.shelfServiceMax(Double.parseDouble(props.getProperty("shelf.service.max")));
        }
        if (props.containsKey("delivery.service.time")) {
            builder.deliveryServiceTime(Double.parseDouble(props.getProperty("delivery.service.time")));
        }
        if (props.containsKey("simulation.duration")) {
            builder.simulationDuration(Double.parseDouble(props.getProperty("simulation.duration")));
        }
        return builder.build();
    }

    public static void writeStatistics(Path outputFile, SimulationStatistics statistics) throws IOException {
        if (statistics == null) {
            throw new IllegalArgumentException("statistics must not be null");
        }
        Path parent = outputFile.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
            writer.write("# Simulation Summary");
            writer.newLine();
            writer.write("Metric;Value");
            writer.newLine();
            writeSummaryLine(writer, "Simulation time", statistics.getSimulationTime());
            writeSummaryLine(writer, "Total arrivals", statistics.getTotalArrivals());
            writeSummaryLine(writer, "Total departures", statistics.getTotalDepartures());
            writeSummaryLine(writer, "Average waiting time", statistics.getAverageWaitingTime());
            writeSummaryLine(writer, "Average response time", statistics.getAverageResponseTime());
            writeSummaryLine(writer, "Average service time", statistics.getAverageServiceTimePerCustomer());
            writeSummaryLine(writer, "Throughput", statistics.getThroughput());
            writeSummaryLine(writer, "Average number in system", statistics.getAverageNumberInSystem());
            writer.newLine();
            writer.write("# Service Point Statistics");
            writer.newLine();
            writer.write("Name;Arrivals;Completions;Utilization;Throughput;AvgServiceTime");
            writer.newLine();
            for (ServicePointStatistics stats : statistics.getServicePointStatistics()) {
                writer.write(String.format(Locale.US, "%s;%d;%d;%.3f;%.3f;%.3f",
                        stats.getServicePointName(),
                        stats.getArrivals(),
                        stats.getCompletions(),
                        stats.getUtilization(statistics.getSimulationTime()),
                        stats.getThroughput(statistics.getSimulationTime()),
                        stats.getAverageServiceTime()));
                writer.newLine();
            }
        }
    }

    private static void writeSummaryLine(BufferedWriter writer, String label, double value) throws IOException {
        writer.write(String.format(Locale.US, "%s;%.3f", label, value));
        writer.newLine();
    }

    private static void writeSummaryLine(BufferedWriter writer, String label, int value) throws IOException {
        writer.write(String.format(Locale.US, "%s;%d", label, value));
        writer.newLine();
    }
}
