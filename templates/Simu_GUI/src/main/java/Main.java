import controller.IControllerMtoV;
import simu.model.MyEngine;

import simu.framework.Trace;

public class Main {
    public static void main(String[] args) {
        // Set trace level
        Trace.setTraceLevel(Trace.Level.INFO);

        IControllerMtoV controller = new IControllerMtoV() {
            @Override
            public void visualiseCustomer() {
                // simple terminal visualization
            }

            @Override
            public void showEndTime(double time) {
                System.out.println("Simulation ended at: " + time);
            }
        };

        MyEngine engine = new MyEngine(controller);
        engine.setSimulationTime(200); // simulation 200 time units
        engine.setDelay(10);           // delay for visualization
        engine.start();
    }
}
