package simu.model;

import eduni.distributions.ContinuousGenerator;
import simu.framework.Clock;
import simu.framework.Event;
import simu.framework.EventList;

import java.util.LinkedList;

public class ServicePoint {
    private LinkedList<Customer> queue = new LinkedList<>();
    private ContinuousGenerator generator;
    private EventList eventList;
    private EventType eventTypeScheduled;
    private boolean reserved = false;

    public ServicePoint(ContinuousGenerator generator, EventList eventList, EventType type) {
        this.generator = generator;
        this.eventList = eventList;
        this.eventTypeScheduled = type;
    }

    public void addQueue(Customer c) {
        queue.add(c);
    }

    public Customer removeQueue() {
        reserved = false;
        return queue.poll();
    }

    public void beginService() {
        reserved = true;
        if (!queue.isEmpty()) {
            double serviceTime = generator.sample();
            eventList.add(new Event(eventTypeScheduled, Clock.getInstance().getTime() + serviceTime));
        }
    }

    public boolean isReserved() { return reserved; }
    public boolean isOnQueue() { return !queue.isEmpty(); }
}
