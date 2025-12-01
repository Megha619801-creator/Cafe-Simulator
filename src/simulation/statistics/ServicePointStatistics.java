package simulation.statistics;

/**
 * Immutable statistics snapshot for a single service point.
 */
public final class ServicePointStatistics {

    private final String servicePointName;
    private final int arrivals;
    private final int completions;
    private final double totalServiceTime;

    public ServicePointStatistics(String servicePointName,
                                  int arrivals,
                                  int completions,
                                  double totalServiceTime) {
        this.servicePointName = servicePointName;
        this.arrivals = arrivals;
        this.completions = completions;
        this.totalServiceTime = totalServiceTime;
    }

    public String getServicePointName() {
        return servicePointName;
    }

    public int getArrivals() {
        return arrivals;
    }

    public int getCompletions() {
        return completions;
    }

    public double getTotalServiceTime() {
        return totalServiceTime;
    }

    public double getUtilization(double simulationTime) {
        if (simulationTime <= 0) {
            return 0.0;
        }
        return totalServiceTime / simulationTime;
    }

    public double getThroughput(double simulationTime) {
        if (simulationTime <= 0) {
            return 0.0;
        }
        return completions / simulationTime;
    }

    public double getAverageServiceTime() {
        if (completions == 0) {
            return 0.0;
        }
        return totalServiceTime / completions;
    }
}

