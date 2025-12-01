package simulation.logic;

import eduni.distributions.Negexp;
import eduni.distributions.Uniform;
import simulation.config.SimulationParameters;
import simulation.model.Clock;
import simulation.model.Customer;
import simulation.model.Event;
import simulation.model.EventList;
import simulation.model.ServicePoint;
import simulation.random.ArrivalProcess;
import simulation.random.DeterministicGenerator;
import simulation.random.PositiveNormalGenerator;
import simulation.statistics.SimulationStatistics;
import simulation.statistics.StatisticsCollector;

import java.util.ArrayList;
import java.util.List;

public class Simulator {
    private final EventList eventList = new EventList();
    private final Clock clock = Clock.getInstance();
    private final SimulationParameters parameters;

    // Service points
    private final ServicePoint cashier;
    private final ServicePoint barista;
    private final ServicePoint shelf;
    private final ServicePoint delivery;

    private final ArrivalProcess instoreArrivalProcess;
    private final ArrivalProcess mobileArrivalProcess;

    private final List<SimulationListener> listeners = new ArrayList<>();
    private final StatisticsCollector statisticsCollector = new StatisticsCollector();

    public Simulator() {
        this(SimulationParameters.defaults());
    }

    public Simulator(SimulationParameters parameters) {
        this.parameters = parameters;
        this.cashier = new ServicePoint("Cashier", new Negexp(parameters.getCashierServiceMean()));
        this.barista = new ServicePoint("Barista",
                new PositiveNormalGenerator(parameters.getBaristaServiceMean(),
                        parameters.getBaristaServiceVariance()));
        this.shelf = new ServicePoint("Pickup Shelf",
                new Uniform(parameters.getShelfServiceMin(), parameters.getShelfServiceMax()));
        this.delivery = new ServicePoint("Delivery Window",
                new DeterministicGenerator(parameters.getDeliveryServiceTime()));
        this.instoreArrivalProcess = new ArrivalProcess("INSTORE", cashier,
                new Negexp(parameters.getInstoreArrivalMean()));
        this.mobileArrivalProcess = new ArrivalProcess("MOBILE", barista,
                new Negexp(parameters.getMobileArrivalMean()));
        listeners.add(new ConsoleSimulationListener());
        statisticsCollector.registerServicePoint(cashier, false);
        statisticsCollector.registerServicePoint(barista, false);
        statisticsCollector.registerServicePoint(shelf, true);
        statisticsCollector.registerServicePoint(delivery, true);
        listeners.add(statisticsCollector);
    }

    public void addListener(SimulationListener listener) {
        listeners.add(listener);
    }

    public void initialize() {
        clock.reset();
        statisticsCollector.reset();

        // First arrivals for each customer type
        instoreArrivalProcess.scheduleNext(clock.getTime(), eventList);
        mobileArrivalProcess.scheduleNext(clock.getTime(), eventList);

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

    public void run() {
        run(parameters.getSimulationDuration());
    }

    // B-phase: handle scheduled ARRIVAL and DEPARTURE events
    private void handleBEvent(Event e) {
        Customer c = e.getCustomer();
        ServicePoint sp = e.getTarget();

        if (e.getType() == Event.ARRIVAL) {
            notifyArrival(e);
            // Customer arrives to the queue of a service point
            sp.addCustomer(c);

            if (c.getType().equals("INSTORE")) {
                instoreArrivalProcess.scheduleNext(clock.getTime(), eventList);
            } else if (c.getType().equals("MOBILE")) {
                mobileArrivalProcess.scheduleNext(clock.getTime(), eventList);
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

    public SimulationStatistics getStatistics() {
        return statisticsCollector.snapshot(clock.getTime());
    }
}
