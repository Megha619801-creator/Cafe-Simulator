package simulation.model;

import java.util.PriorityQueue;

public class EventList {
    private final PriorityQueue<Event> list = new PriorityQueue<>();

    public void add(Event e) {
        list.add(e);
    }

    public Event removeNext() {
        return list.poll();
    }

    public Event peekNext() {
        return list.peek();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }
}
