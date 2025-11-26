package simulation.logic;

import eduni.distributions.ContinuousGenerator;
import eduni.distributions.Negexp;
import simulation.model.Clock;
import simulation.model.Customer;
import simulation.model.Event;
import simulation.model.EventList;
import simulation.model.ServicePoint;

import java.util.ArrayList;
import java.util.List;

public class Simulator {
    private final EventList eventList = new EventList();
    private final Clock clock = Clock.getInstance();

    // Service points
    private final ServicePoint cashier = new ServicePoint("Cashier", new Negexp(3.0));
    private final ServicePoint barista = new ServicePoint("Barista", new Negexp(5.0));
    private final ServicePoint shelf = new ServicePoint("Pickup Shelf", new Negexp(2.0));
    private final ServicePoint delivery = new ServicePoint("Delivery Window", new Negexp(4.0));

    private final double meanArrivalInstore = 4.0;
    private final double meanArrivalMobile = 6.0;

    private final ContinuousGenerator instoreArrivalGenerator = new Negexp(meanArrivalInstore);
    private final ContinuousGenerator mobileArrivalGenerator = new Negexp(meanArrivalMobile);

    private final List<SimulationListener> listeners = new ArrayList<>();

    public Simulator() {
        listeners.add(new ConsoleSimulationListener());
    }

    public void addListener(SimulationListener listener) {
        listeners.add(listener);
    }

    public void initialize() {
        clock.reset();

        // First arrivals for each customer type
        double firstInstoreArrival = clock.getTime() + instoreArrivalGenerator.sample();
        double firstMobileArrival = clock.getTime() + mobileArrivalGenerator.sample();
        eventList.add(new Event(firstInstoreArrival, Event.ARRIVAL,
                new Customer("INSTORE", firstInstoreArrival), cashier));
        eventList.add(new Event(firstMobileArrival, Event.ARRIVAL,
                new Customer("MOBILE", firstMobileArrival), barista));

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
            notifyArrival(e);
            // Customer arrives to the queue of a service point
            sp.addCustomer(c);

            // External arrivals schedule next external arrival
            if (c.getType().equals("INSTORE")) {
                double nextArrival = clock.getTime() + instoreArrivalGenerator.sample();
                eventList.add(new Event(nextArrival, Event.ARRIVAL,
                        new Customer("INSTORE", nextArrival), cashier));
            } else if (c.getType().equals("MOBILE")) {
                double nextArrival = clock.getTime() + mobileArrivalGenerator.sample();
                eventList.add(new Event(nextArrival, Event.ARRIVAL,
                        new Customer("MOBILE", nextArrival), barista));
            }
        } else if (e.getType() == Event.DEPARTURE) {
            notifyDeparture(e, c.getWaitingTime(), c.getServiceTime());

            // Service at this point has finished
            sp.setBusy(false);

            // Route customers after service by placing them into the next queue
            if (sp == cashier) {
                barista.addCustomer(c);
                notifyRouting(c, sp, barista);
            } else if (sp == barista) {
                if (c.getType().equals("INSTORE")) {
                    shelf.addCustomer(c);
                    notifyRouting(c, sp, shelf);
                } else {
                    delivery.addCustomer(c);
                    notifyRouting(c, sp, delivery);
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

    private void notifyArrival(Event event) {
        for (SimulationListener listener : listeners) {
            listener.onEvent(event);
            listener.onArrival(event);
        }
    }

    private void notifyDeparture(Event event, double waitTime, double serviceTime) {
        for (SimulationListener listener : listeners) {
            listener.onEvent(event);
            listener.onDeparture(event, waitTime, serviceTime);
        }
    }

    private void notifyRouting(Customer customer, ServicePoint from, ServicePoint to) {
        for (SimulationListener listener : listeners) {
            listener.onRouting(customer, from, to);
        }
    }
}
