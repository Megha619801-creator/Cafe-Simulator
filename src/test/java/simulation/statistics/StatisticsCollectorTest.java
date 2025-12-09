package simulation.statistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import simulation.model.Customer;
import simulation.model.Event;
import simulation.model.ServicePoint;

import static org.junit.jupiter.api.Assertions.*;

class StatisticsCollectorTest {

    private StatisticsCollector stats;
    private ServicePoint terminalA;
    private ServicePoint terminalB;

    @BeforeEach
    void setUp() {
        stats = new StatisticsCollector();
        terminalA = new ServicePoint("A", new simulation.random.DeterministicGenerator(1.0));
        terminalB = new ServicePoint("B", new simulation.random.DeterministicGenerator(1.5));
        stats.registerServicePoint(terminalA, true);
        stats.registerServicePoint(terminalB, true);
        stats.reset();
    }

    @Test
    void aggregatesSystemAndPerServicePointMetrics() {
        // customer 1 at terminalA
        Customer c1 = new Customer("INSTORE", 0.0);
        c1.setServiceStartTime(0.0);
        c1.setServiceEndTime(2.0); // serviceTime = 2.0, wait = 0.0, response = 2.0
        Event arrivalA = new Event(0.0, Event.ARRIVAL, c1, terminalA);
        stats.onArrival(arrivalA);
        Event depA = new Event(2.0, Event.DEPARTURE, c1, terminalA);
        stats.onDeparture(depA, c1.getWaitingTime(), c1.getServiceTime());

        // customer 2 at terminalB
        Customer c2 = new Customer("MOBILE", 1.0);
        c2.setServiceStartTime(2.0);
        c2.setServiceEndTime(5.0); // serviceTime = 3.0, wait = 1.0, response = 4.0
        Event arrivalB = new Event(1.0, Event.ARRIVAL, c2, terminalB);
        stats.onArrival(arrivalB);
        Event depB = new Event(5.0, Event.DEPARTURE, c2, terminalB);
        stats.onDeparture(depB, c2.getWaitingTime(), c2.getServiceTime());

        SimulationStatistics snapshot = stats.snapshot(10.0);

        assertEquals(2, snapshot.getTotalArrivals());
        assertEquals(2, snapshot.getTotalDepartures());
        assertEquals(5.0, snapshot.getTotalServiceTime(), 1e-9);
        assertEquals(1.0, snapshot.getTotalWaitTime(), 1e-9);
        assertEquals(6.0, snapshot.getTotalResponseTime(), 1e-9);

        assertEquals(0.5, snapshot.getAverageWaitingTime(), 1e-9);
        assertEquals(2.5, snapshot.getAverageServiceTimePerCustomer(), 1e-9);
        assertEquals(3.0, snapshot.getAverageResponseTime(), 1e-9);
        assertEquals(0.2, snapshot.getThroughput(), 1e-9);
        assertEquals(0.6, snapshot.getAverageNumberInSystem(), 1e-9);
        assertEquals(0.5, snapshot.getSystemUtilization(), 1e-9);

        ServicePointStatistics aStats = snapshot.getServicePointStatistics()
                .stream()
                .filter(s -> s.getServicePointName().equals("A"))
                .findFirst()
                .orElseThrow();
        ServicePointStatistics bStats = snapshot.getServicePointStatistics()
                .stream()
                .filter(s -> s.getServicePointName().equals("B"))
                .findFirst()
                .orElseThrow();

        assertEquals(1, aStats.getArrivals());
        assertEquals(1, aStats.getCompletions());
        assertEquals(2.0, aStats.getTotalServiceTime(), 1e-9);
        assertEquals(0.2, aStats.getThroughput(10.0), 1e-9);
        assertEquals(0.2, aStats.getUtilization(10.0), 1e-9);

        assertEquals(1, bStats.getArrivals());
        assertEquals(1, bStats.getCompletions());
        assertEquals(3.0, bStats.getTotalServiceTime(), 1e-9);
        assertEquals(0.1, bStats.getThroughput(10.0), 1e-9);
        assertEquals(0.3, bStats.getUtilization(10.0), 1e-9);
    }
}

