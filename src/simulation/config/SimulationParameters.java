package simulation.config;

/**
 * Immutable container for all configurable model parameters.
 */
public final class SimulationParameters {

    private final double instoreArrivalMean;
    private final double mobileArrivalMean;
    private final double cashierServiceMean;
    private final double baristaServiceMean;
    private final double baristaServiceVariance;
    private final double shelfServiceMin;
    private final double shelfServiceMax;
    private final double deliveryServiceTime;
    private final double simulationDuration;

    private SimulationParameters(Builder builder) {
        this.instoreArrivalMean = builder.instoreArrivalMean;
        this.mobileArrivalMean = builder.mobileArrivalMean;
        this.cashierServiceMean = builder.cashierServiceMean;
        this.baristaServiceMean = builder.baristaServiceMean;
        this.baristaServiceVariance = builder.baristaServiceVariance;
        this.shelfServiceMin = builder.shelfServiceMin;
        this.shelfServiceMax = builder.shelfServiceMax;
        this.deliveryServiceTime = builder.deliveryServiceTime;
        this.simulationDuration = builder.simulationDuration;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static SimulationParameters defaults() {
        return builder().build();
    }

    public double getInstoreArrivalMean() {
        return instoreArrivalMean;
    }

    public double getMobileArrivalMean() {
        return mobileArrivalMean;
    }

    public double getCashierServiceMean() {
        return cashierServiceMean;
    }

    public double getBaristaServiceMean() {
        return baristaServiceMean;
    }

    public double getBaristaServiceVariance() {
        return baristaServiceVariance;
    }

    public double getShelfServiceMin() {
        return shelfServiceMin;
    }

    public double getShelfServiceMax() {
        return shelfServiceMax;
    }

    public double getDeliveryServiceTime() {
        return deliveryServiceTime;
    }

    public double getSimulationDuration() {
        return simulationDuration;
    }

    public static final class Builder {
        private double instoreArrivalMean = 4.0;
        private double mobileArrivalMean = 6.0;
        private double cashierServiceMean = 3.0;
        private double baristaServiceMean = 4.5;
        private double baristaServiceVariance = 1.2;
        private double shelfServiceMin = 1.0;
        private double shelfServiceMax = 2.5;
        private double deliveryServiceTime = 4.0;
        private double simulationDuration = 60.0;

        public Builder instoreArrivalMean(double value) {
            this.instoreArrivalMean = value;
            return this;
        }

        public Builder mobileArrivalMean(double value) {
            this.mobileArrivalMean = value;
            return this;
        }

        public Builder cashierServiceMean(double value) {
            this.cashierServiceMean = value;
            return this;
        }

        public Builder baristaServiceMean(double value) {
            this.baristaServiceMean = value;
            return this;
        }

        public Builder baristaServiceVariance(double value) {
            this.baristaServiceVariance = value;
            return this;
        }

        public Builder shelfServiceMin(double value) {
            this.shelfServiceMin = value;
            return this;
        }

        public Builder shelfServiceMax(double value) {
            this.shelfServiceMax = value;
            return this;
        }

        public Builder deliveryServiceTime(double value) {
            this.deliveryServiceTime = value;
            return this;
        }

        public Builder simulationDuration(double value) {
            this.simulationDuration = value;
            return this;
        }

        public SimulationParameters build() {
            if (shelfServiceMax <= shelfServiceMin) {
                throw new IllegalArgumentException("Shelf service max must exceed min.");
            }
            if (instoreArrivalMean <= 0 || mobileArrivalMean <= 0) {
                throw new IllegalArgumentException("Arrival means must be positive.");
            }
            if (cashierServiceMean <= 0 || baristaServiceMean <= 0 || deliveryServiceTime <= 0) {
                throw new IllegalArgumentException("Service times must be positive.");
            }
            if (baristaServiceVariance <= 0) {
                throw new IllegalArgumentException("Barista variance must be positive.");
            }
            if (simulationDuration <= 0) {
                throw new IllegalArgumentException("Simulation duration must be positive.");
            }
            return new SimulationParameters(this);
        }
    }
}
