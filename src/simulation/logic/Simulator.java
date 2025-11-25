package simulation.logic;

import simulation.model.Clock;
import simulation.model.Customer;
import simulation.model.Event;
import simulation.model.EventList;
import simulation.model.ServicePoint;

import java.util.Random;

public class Simulator {
    private final EventList eventList = new EventList();
    private final Clock clock = Clock.getInstance();

    // Service points
    private final ServicePoint cashier = new ServicePoint("Cashier", 3.0);
    private final ServicePoint barista = new ServicePoint("Barista", 5.0);
    private final ServicePoint shelf = new ServicePoint("Pickup Shelf", 2.0);
    private final ServicePoint delivery = new ServicePoint("Delivery Window", 4.0);

    private final Random rand = new Random();

    private final double meanArrivalInstore = 4.0;
    private final double meanArrivalMobile = 6.0;

    public void initialize() {
        clock.reset();

        // First arrivals for each customer type
        eventList.add(new Event(generateArrivalTime(meanArrivalInstore), Event.ARRIVAL,
                new Customer("INSTORE", clock.getTime()), cashier));
        eventList.add(new Event(generateArrivalTime(meanArrivalMobile), Event.ARRIVAL,
                new Customer("MOBILE", clock.getTime()), barista));
    }

    public void run(double endTime) {
        while (!eventList.isEmpty() && clock.getTime() < endTime) {
            Event e = eventList.removeNext();
            clock.setTime(e.getTime());
            handleEvent(e);
        }
    }

    private void handleEvent(Event e) {
        switch (e.getType()) {
            case Event.ARRIVAL -> handleArrival(e);
            case Event.DEPARTURE -> handleDeparture(e);
        }
    }

    private void handleArrival(Event e) {
        Customer c = e.getCustomer();
        ServicePoint sp = e.getTarget();
        System.out.println(e);

        if (!sp.isBusy()) {
            sp.setBusy(true);
            double currentTime = clock.getTime();
            c.setServiceStartTime(currentTime);
            double serviceTime = sp.generateServiceTime();
            c.setServiceEndTime(currentTime + serviceTime);
            eventList.add(new Event(currentTime + serviceTime, Event.DEPARTURE, c, sp));
        } else {
            sp.addCustomer(c);
        }

        // Schedule next arrival depending on type
        if (c.getType().equals("INSTORE")) {
            double nextArrival = clock.getTime() + generateArrivalTime(meanArrivalInstore);
            eventList.add(new Event(nextArrival, Event.ARRIVAL,
                    new Customer("INSTORE", nextArrival), cashier));
        } else if (c.getType().equals("MOBILE")) {
            double nextArrival = clock.getTime() + generateArrivalTime(meanArrivalMobile);
            eventList.add(new Event(nextArrival, Event.ARRIVAL,
                    new Customer("MOBILE", nextArrival), barista));
        }
    }

    private void handleDeparture(Event e) {
        Customer c = e.getCustomer();
        ServicePoint sp = e.getTarget();
        System.out.println(e + " (Wait=" + c.getWaitingTime() + ", Service=" + c.getServiceTime() + ")");

        if (sp.hasWaitingCustomer()) {
            Customer next = sp.getNextCustomer();
            double currentTime = clock.getTime();
            next.setServiceStartTime(currentTime);
            double serviceTime = sp.generateServiceTime();
            next.setServiceEndTime(currentTime + serviceTime);
            eventList.add(new Event(currentTime + serviceTime, Event.DEPARTURE, next, sp));
        } else {
            sp.setBusy(false);
        }

        // Route customers after service
        if (sp == cashier) {
            eventList.add(new Event(clock.getTime(), Event.ARRIVAL, c, barista));
        } else if (sp == barista) {
            if (c.getType().equals("INSTORE")) {
                eventList.add(new Event(clock.getTime(), Event.ARRIVAL, c, shelf));
            } else {
                eventList.add(new Event(clock.getTime(), Event.ARRIVAL, c, delivery));
            }
        }
    }

    private double generateArrivalTime(double mean) {
        return -mean * Math.log(1 - rand.nextDouble());
    }
}
