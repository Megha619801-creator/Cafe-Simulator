package simu.model;

import eduni.distributions.ContinuousGenerator;
import simulation.model.Customer;
import simu.framework.Clock;
import simu.framework.Event;
import simu.framework.EventList;

import java.util.LinkedList;

public class ServicePoint {
    private final LinkedList<Customer> queue = new LinkedList<>();
    private final ContinuousGenerator generator;
    private final EventList eventList;
    private final EventType eventTypeScheduled;
    private boolean reserved = false;

    private int arrivals;
    private int completions;
    private double busyTime;
    private double lastServiceStart = Double.NaN;

    public ServicePoint(ContinuousGenerator generator, EventList eventList, EventType type) {
        this.generator = generator;
        this.eventList = eventList;
        this.eventTypeScheduled = type;
    }

    public void addQueue(Customer c) {
        queue.add(c);
        arrivals++;
    }

    public Customer removeQueue() {
        double currentTime = Clock.getInstance().getTime();
        finalizeOngoingService(currentTime);
        reserved = false;
        Customer customer = queue.poll();
        if (customer != null) {
            completions++;
            customer.setServiceEndTime(currentTime);
        }
        return customer;
    }

    public void beginService() {
        if (!queue.isEmpty()) {
            reserved = true;
            lastServiceStart = Clock.getInstance().getTime();
            if (!queue.isEmpty()) {
                // peek to avoid removing before scheduling
                queue.peek().setServiceStartTime(lastServiceStart);
            }
            double serviceTime = generator.sample();
            eventList.add(new Event(eventTypeScheduled, lastServiceStart + serviceTime));
        }
    }

    public void finalizeBusyTime(double currentTime) {
        finalizeOngoingService(currentTime);
    }

    private void finalizeOngoingService(double currentTime) {
        if (reserved && !Double.isNaN(lastServiceStart)) {
            busyTime += Math.max(0.0, currentTime - lastServiceStart);
            lastServiceStart = Double.NaN;
        }
    }

    public boolean isReserved() {
        return reserved;
    }

    public boolean isOnQueue() {
        return !queue.isEmpty();
    }

    public int getArrivals() {
        return arrivals;
    }

    public int getCompletions() {
        return completions;
    }

    public double getBusyTime() {
        return busyTime;
    }

    public EventType getEventType() {
        return eventTypeScheduled;
    }
}
