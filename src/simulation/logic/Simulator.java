package simulation.logic;

import simulation.model.*;
import java.util.Random;

public class Simulator {
    private final EventList eventList = new EventList();
    private double clock = 0.0;

    // Service points
    private final ServicePoint cashier = new ServicePoint("Cashier", 3.0);
    private final ServicePoint barista = new ServicePoint("Barista", 5.0);
    private final ServicePoint shelf = new ServicePoint("Pickup Shelf", 2.0);
    private final ServicePoint delivery = new ServicePoint("Delivery Window", 4.0);

    private final Random rand = new Random();

    private final double meanArrivalInstore = 4.0;
    private final double meanArrivalMobile = 6.0;

    public void initialize() {
        // First arrivals for each customer type
        eventList.add(new Event(generateArrivalTime(meanArrivalInstore), Event.ARRIVAL,
                new Customer("INSTORE", clock), cashier));
        eventList.add(new Event(generateArrivalTime(meanArrivalMobile), Event.ARRIVAL,
                new Customer("MOBILE", clock), barista));
    }

    public void run(double endTime) {
        while (!eventList.isEmpty() && clock < endTime) {
            Event e = eventList.removeNext();
            clock = e.getTime();
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
            c.setServiceStartTime(clock);
            double serviceTime = sp.generateServiceTime();
            c.setServiceEndTime(clock + serviceTime);
            eventList.add(new Event(clock + serviceTime, Event.DEPARTURE, c, sp));
        } else {
            sp.addCustomer(c);
        }

        // Schedule next arrival depending on type
        if (c.getType().equals("INSTORE")) {
            double nextArrival = clock + generateArrivalTime(meanArrivalInstore);
            eventList.add(new Event(nextArrival, Event.ARRIVAL,
                    new Customer("INSTORE", nextArrival), cashier));
        } else if (c.getType().equals("MOBILE")) {
            double nextArrival = clock + generateArrivalTime(meanArrivalMobile);
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
            next.setServiceStartTime(clock);
            double serviceTime = sp.generateServiceTime();
            next.setServiceEndTime(clock + serviceTime);
            eventList.add(new Event(clock + serviceTime, Event.DEPARTURE, next, sp));
        } else {
            sp.setBusy(false);
        }

        // Route customers after service
        if (sp == cashier) {
            eventList.add(new Event(clock, Event.ARRIVAL, c, barista));
        } else if (sp == barista) {
            if (c.getType().equals("INSTORE")) {
                eventList.add(new Event(clock, Event.ARRIVAL, c, shelf));
            } else {
                eventList.add(new Event(clock, Event.ARRIVAL, c, delivery));
            }
        }
    }

    private double generateArrivalTime(double mean) {
        return -mean * Math.log(1 - rand.nextDouble());
    }
}
