package simulation.random;

import eduni.distributions.ContinuousGenerator;
import simulation.model.Customer;
import simulation.model.Event;
import simulation.model.EventList;
import simulation.model.ServicePoint;

/**
 * Generates arrival events using its own random generator.
 */
public class ArrivalProcess {

    private final String customerType;
    private final ServicePoint target;
    private final ContinuousGenerator interArrivalGenerator;

    public ArrivalProcess(String customerType, ServicePoint target, ContinuousGenerator generator) {
        this.customerType = customerType;
        this.target = target;
        this.interArrivalGenerator = generator;
    }

    public void scheduleNext(double currentTime, EventList eventList) {
        double nextTime = currentTime + interArrivalGenerator.sample();
        eventList.add(new Event(nextTime, Event.ARRIVAL,
                new Customer(customerType, nextTime), target));
    }
}
