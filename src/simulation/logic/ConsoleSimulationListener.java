package simulation.logic;

import simulation.model.Customer;
import simulation.model.Event;
import simulation.model.ServicePoint;

/**
 * Simple listener that logs simulation events to the console.
 */
public class ConsoleSimulationListener implements SimulationListener {

    @Override
    public void onEvent(Event event) {
        System.out.println(event);
    }

    @Override
    public void onDeparture(Event event, double waitTime, double serviceTime) {
        System.out.println(event + " (Wait=" + waitTime + ", Service=" + serviceTime + ")");
    }

    @Override
    public void onRouting(Customer customer, ServicePoint from, ServicePoint to) {
        String destination = (to != null) ? to.getName() : "OUT";
        System.out.printf("Routing %s from %s to %s%n", customer, from.getName(), destination);
    }
}
