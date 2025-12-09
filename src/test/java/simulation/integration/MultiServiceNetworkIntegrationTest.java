package simulation.integration;

import org.junit.jupiter.api.Test;
import simulation.model.Clock;
import simulation.model.Customer;
import simulation.model.Event;
import simulation.model.EventList;
import simulation.model.ServicePoint;
import simulation.random.ArrivalProcess;
import simulation.random.DeterministicGenerator;
import simulation.statistics.SimulationStatistics;
import simulation.statistics.StatisticsCollector;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration-style check with 4 service points and branching paths
 * to align with the project requirement of a non-linear network.
 */
class MultiServiceNetworkIntegrationTest {

    @Test
    void fourServicePointsBranchingScenarioProducesExpectedStats() {
        Clock clock = Clock.getInstance();
        clock.reset();

        // Entry -> {PrepA, PrepB} -> Pickup
        ServicePoint entry = new ServicePoint("Entry", new DeterministicGenerator(0.5));
        ServicePoint prepA = new ServicePoint("PrepA", new DeterministicGenerator(0.7));
        ServicePoint prepB = new ServicePoint("PrepB", new DeterministicGenerator(0.4));
        ServicePoint pickup = new ServicePoint("Pickup", new DeterministicGenerator(0.3));

        StatisticsCollector stats = new StatisticsCollector();
        stats.registerServicePoint(entry, false);
        stats.registerServicePoint(prepA, false);
        stats.registerServicePoint(prepB, false);
        stats.registerServicePoint(pickup, true); // terminal
        stats.reset();

        ArrivalProcess arrivalProcess = new ArrivalProcess("INSTORE", entry, new DeterministicGenerator(1.0));
        EventList eventList = new EventList();

        // schedule first arrival at t=1.0
        arrivalProcess.scheduleNext(clock.getTime(), eventList);

        double endTime = 5.0;

        while (!eventList.isEmpty() && clock.getTime() <= endTime) {
            Event e = eventList.removeNext();
            double eventTime = e.getTime();
            if (eventTime > endTime) {
                break;
            }
            clock.setTime(eventTime);

            if (e.getType() == Event.ARRIVAL) {
                stats.onArrival(e);
                e.getTarget().addCustomer(e.getCustomer());
                startServiceIfIdle(e.getTarget(), eventList, clock);
                // schedule next external arrival
                arrivalProcess.scheduleNext(clock.getTime(), eventList);
            } else {
                stats.onDeparture(e, e.getCustomer().getWaitingTime(), e.getCustomer().getServiceTime());
                e.getTarget().setBusy(false);

                if (e.getTarget() == entry) {
                    // route from entry to one of the prep points
                    int id = e.getCustomer().getId();
                    ServicePoint nextPrep = (id <= 2) ? prepA : prepB;
                    stats.onRouting(e.getCustomer(), entry, nextPrep);
                    nextPrep.addCustomer(e.getCustomer());
                    startServiceIfIdle(nextPrep, eventList, clock);
                } else if (e.getTarget() == prepA || e.getTarget() == prepB) {
                    // route from prep points to pickup
                    stats.onRouting(e.getCustomer(), e.getTarget(), pickup);
                    pickup.addCustomer(e.getCustomer());
                    startServiceIfIdle(pickup, eventList, clock);
                }

                // continue serving next in the same service point if any
                startServiceIfIdle(e.getTarget(), eventList, clock);
            }
        }

        SimulationStatistics snapshot = stats.snapshot(clock.getTime());

        assertEquals(5, snapshot.getTotalArrivals()); // arrivals at t=1,2,3,4,5
        assertEquals(3, snapshot.getTotalDepartures());
        assertEquals(3.6, snapshot.getTotalServiceTime(), 1e-9); // three completed customers through all stages

        assertTrue(snapshot.getAverageWaitingTime() >= 0.0);
        assertEquals(0.6, snapshot.getThroughput(), 1e-9);
        assertEquals(0.72, snapshot.getSystemUtilization(), 1e-9);
    }

    private void startServiceIfIdle(ServicePoint sp, EventList eventList, Clock clock) {
        if (sp.isBusy() || !sp.hasWaitingCustomer()) {
            return;
        }
        Customer next = sp.getNextCustomer();
        double now = clock.getTime();
        next.setServiceStartTime(now);
        double serviceTime = sp.generateServiceTime();
        next.setServiceEndTime(now + serviceTime);
        sp.setBusy(true);
        eventList.add(new Event(now + serviceTime, Event.DEPARTURE, next, sp));
    }
}

