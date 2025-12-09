package simulation.random;

import org.junit.jupiter.api.Test;
import simulation.model.Customer;
import simulation.model.Event;
import simulation.model.EventList;
import simulation.model.ServicePoint;

import static org.junit.jupiter.api.Assertions.*;

class ArrivalProcessTest {

    @Test
    void scheduleNextAddsArrivalWithExpectedTimeAndCustomer() {
        EventList eventList = new EventList();
        ServicePoint sp = new ServicePoint("Entry", new DeterministicGenerator(1.0));
        ArrivalProcess process = new ArrivalProcess("INSTORE", sp, new DeterministicGenerator(2.0));

        double now = 5.0;
        process.scheduleNext(now, eventList);

        assertFalse(eventList.isEmpty());
        Event next = eventList.removeNext();
        assertEquals(now + 2.0, next.getTime(), 1e-9);
        assertEquals(Event.ARRIVAL, next.getType());
        assertSame(sp, next.getTarget());

        Customer c = next.getCustomer();
        assertEquals("INSTORE", c.getType());
        assertEquals(now + 2.0, c.getArrivalTime(), 1e-9);
    }
}

