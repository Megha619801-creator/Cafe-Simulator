package simulation.logic;

import simulation.model.Customer;
import simulation.model.Event;
import simulation.model.ServicePoint;

/**
 * Listener interface for observing simulation events without tying the core
 * logic to a UI.
 */
public interface SimulationListener {

    default void onEvent(Event event) {
    }

    default void onArrival(Event event) {
    }

    default void onDeparture(Event event, double waitTime, double serviceTime) {
    }

    default void onRouting(Customer customer, ServicePoint from, ServicePoint to) {
    }
}
