package simulation.logic;

import simulation.config.SimulationParameters;

public class SimulatorMain {
    public static void main(String[] args) {
        SimulationParameters params = SimulationParameters.builder()
                .instoreArrivalMean(4.0)
                .mobileArrivalMean(6.0)
                .cashierServiceMean(3.0)
                .baristaServiceMean(4.5)
                .baristaServiceVariance(1.2)
                .shelfServiceMin(1.0)
                .shelfServiceMax(2.5)
                .deliveryServiceTime(4.0)
                .simulationDuration(60.0)
                .build();

        Simulator sim = new Simulator(params);
        sim.initialize();
        sim.run();
    }
}
