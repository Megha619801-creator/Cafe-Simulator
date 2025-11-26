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
            // A-phase: find time of next event and advance clock
            Event first = eventList.removeNext();
            double currentTime = first.getTime();
            if (currentTime > endTime) {
                clock.setTime(endTime);
                break;
            }
            clock.setTime(currentTime);

            // B-phase: execute all bound (scheduled) events due at current time
            handleBEvent(first);
            while (!eventList.isEmpty() && eventList.peekNext().getTime() == currentTime) {
                Event nextAtSameTime = eventList.removeNext();
                handleBEvent(nextAtSameTime);
            }

            // C-phase: repeatedly start services where conditions are met
            boolean executed;
            do {
                executed = cPhase();
            } while (executed);
        }
    }

    // B-phase: handle scheduled ARRIVAL and DEPARTURE events
    private void handleBEvent(Event e) {
        Customer c = e.getCustomer();
        ServicePoint sp = e.getTarget();

        if (e.getType() == Event.ARRIVAL) {
            System.out.println(e);
            // Customer arrives to the queue of a service point
            sp.addCustomer(c);

            // External arrivals schedule next external arrival
            if (c.getType().equals("INSTORE")) {
                double nextArrival = clock.getTime() + generateArrivalTime(meanArrivalInstore);
                eventList.add(new Event(nextArrival, Event.ARRIVAL,
                        new Customer("INSTORE", nextArrival), cashier));
            } else if (c.getType().equals("MOBILE")) {
                double nextArrival = clock.getTime() + generateArrivalTime(meanArrivalMobile);
                eventList.add(new Event(nextArrival, Event.ARRIVAL,
                        new Customer("MOBILE", nextArrival), barista));
            }
        } else if (e.getType() == Event.DEPARTURE) {
            System.out.println(e + " (Wait=" + c.getWaitingTime() + ", Service=" + c.getServiceTime() + ")");

            // Service at this point has finished
            sp.setBusy(false);

            // Route customers after service by placing them into the next queue
            if (sp == cashier) {
                barista.addCustomer(c);
            } else if (sp == barista) {
                if (c.getType().equals("INSTORE")) {
                    shelf.addCustomer(c);
                } else {
                    delivery.addCustomer(c);
                }
            }
            // shelf and delivery are terminal points in this simple model
        }
    }

    // C-phase: for each service point, start service if there is a waiting customer
    // and the point is idle
    private boolean cPhase() {
        boolean executed = false;
        executed |= tryStartService(cashier);
        executed |= tryStartService(barista);
        executed |= tryStartService(shelf);
        executed |= tryStartService(delivery);
        return executed;
    }

    private boolean tryStartService(ServicePoint sp) {
        if (!sp.isBusy() && sp.hasWaitingCustomer()) {
            Customer next = sp.getNextCustomer();
            double currentTime = clock.getTime();
            next.setServiceStartTime(currentTime);
            double serviceTime = sp.generateServiceTime();
            next.setServiceEndTime(currentTime + serviceTime);
            sp.setBusy(true);
            eventList.add(new Event(currentTime + serviceTime, Event.DEPARTURE, next, sp));
            return true;
        }
        return false;
    }

    private double generateArrivalTime(double mean) {
        return -mean * Math.log(1 - rand.nextDouble());
    }
}
