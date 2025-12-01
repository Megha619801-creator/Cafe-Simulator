package simulation.statistics;

import java.util.Collections;
import java.util.List;

/**
 * Immutable snapshot containing system level and per-service point statistics.
 */
public final class SimulationStatistics {

    private final double simulationTime;
    private final List<ServicePointStatistics> servicePointStatistics;
    private final int totalArrivals;
    private final int totalDepartures;
    private final double totalServiceTime;
    private final double totalWaitTime;
    private final double totalResponseTime;

    public SimulationStatistics(double simulationTime,
            List<ServicePointStatistics> servicePointStatistics,
            int totalArrivals,
            int totalDepartures,
            double totalServiceTime,
            double totalWaitTime,
            double totalResponseTime) {
        this.simulationTime = simulationTime;
        this.servicePointStatistics = List.copyOf(servicePointStatistics);
        this.totalArrivals = totalArrivals;
        this.totalDepartures = totalDepartures;
        this.totalServiceTime = totalServiceTime;
        this.totalWaitTime = totalWaitTime;
        this.totalResponseTime = totalResponseTime;
    }

    public double getSimulationTime() {
        return simulationTime;
    }

    public List<ServicePointStatistics> getServicePointStatistics() {
        return Collections.unmodifiableList(servicePointStatistics);
    }

    public int getTotalArrivals() {
        return totalArrivals;
    }

    public int getTotalDepartures() {
        return totalDepartures;
    }

    public double getTotalServiceTime() {
        return totalServiceTime;
    }

    public double getTotalWaitTime() {
        return totalWaitTime;
    }

    public double getTotalResponseTime() {
        return totalResponseTime;
    }

    public double getAverageWaitingTime() {
        if (totalDepartures == 0) {
            return 0.0;
        }
        return totalWaitTime / totalDepartures;
    }

    public double getAverageResponseTime() {
        if (totalDepartures == 0) {
            return 0.0;
        }
        return totalResponseTime / totalDepartures;
    }

    public double getAverageServiceTimePerCustomer() {
        if (totalDepartures == 0) {
            return 0.0;
        }
        return totalServiceTime / totalDepartures;
    }

    public double getThroughput() {
        if (simulationTime <= 0) {
            return 0.0;
        }
        return totalDepartures / simulationTime;
    }

    public double getAverageNumberInSystem() {
        if (simulationTime <= 0) {
            return 0.0;
        }
        return totalResponseTime / simulationTime;
    }
}
