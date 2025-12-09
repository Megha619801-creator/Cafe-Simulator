package simulation.integration;

import org.junit.jupiter.api.Test;
import simulation.model.*;
import simulation.random.DeterministicGenerator;
import simulation.random.ArrivalProcess;
import simulation.statistics.StatisticsCollector;
import simulation.statistics.SimulationStatistics;

import static org.junit.jupiter.api.Assertions.*;

public class StatisticsIntegrationTest {

    @Test
    void deterministicSingleServicePointScenario() {
        EventList eventList = new EventList();
        Clock clock = Clock.getInstance();
        clock.reset();

        // Service point with deterministic service time 0.5 (terminal)
        ServicePoint sp = new ServicePoint("SP", new DeterministicGenerator(0.5));

        // Arrival process deterministic every 1.0 time units
        ArrivalProcess ap = new ArrivalProcess("INSTORE", sp, new DeterministicGenerator(1.0));

        // Statistics collector and register this SP as terminal
        StatisticsCollector stats = new StatisticsCollector();
        stats.registerServicePoint(sp, true);
        stats.reset();

        // Schedule first arrival at time 1.0
        ap.scheduleNext(clock.getTime(), eventList);

        double endTime = 3.0;

        // simple event-loop similar to Simulator.run
        while (!eventList.isEmpty() && clock.getTime() < endTime) {
            Event e = eventList.removeNext();
            double currentTime = e.getTime();
            if (currentTime > endTime) {
                clock.setTime(endTime);
                break;
            }
            clock.setTime(currentTime);

            if (e.getType() == Event.ARRIVAL) {
                // notify stats and place customer
                stats.onEvent(e);
                stats.onArrival(e);
                sp.addCustomer(e.getCustomer());
                // schedule next arrival
                ap.scheduleNext(clock.getTime(), eventList);
            } else {
                // departure
                stats.onEvent(e);
                stats.onDeparture(e, e.getCustomer().getWaitingTime(), e.getCustomer().getServiceTime());
                sp.setBusy(false);
            }

            // C-phase: start service if idle
            if (!sp.isBusy() && sp.hasWaitingCustomer()) {
                Customer next = sp.getNextCustomer();
                double now = clock.getTime();
                next.setServiceStartTime(now);
                double serviceTime = sp.generateServiceTime();
                next.setServiceEndTime(now + serviceTime);
                sp.setBusy(true);
                eventList.add(new Event(now + serviceTime, Event.DEPARTURE, next, sp));
            }
        }

        SimulationStatistics snapshot = stats.snapshot(clock.getTime());

        // Expectations:
        // arrivals at times 1.0, 2.0, 3.0 -> totalArrivals = 3
        assertEquals(3, snapshot.getTotalArrivals());
        // departures processed at 1.5 and 2.5 only (3.5 beyond endTime) -> totalDepartures = 2
        assertEquals(2, snapshot.getTotalDepartures());
        // total service time for terminal departures = 0.5 + 0.5 = 1.0
        assertEquals(1.0, snapshot.getTotalServiceTime(), 1e-9);
        // average service time per customer = 0.5
        assertEquals(0.5, snapshot.getAverageServiceTimePerCustomer(), 1e-9);
        // average waiting time = 0 (service started immediately)
        assertEquals(0.0, snapshot.getAverageWaitingTime(), 1e-9);
    }
}
