package simulation.model;

import eduni.distributions.ContinuousGenerator;

import java.util.LinkedList;
import java.util.Queue;

public class ServicePoint {
    private final String name;
    private final Queue<Customer> queue = new LinkedList<>();
    private boolean busy = false;
    private final ContinuousGenerator serviceTimeGenerator;

    public ServicePoint(String name, ContinuousGenerator serviceTimeGenerator) {
        this.name = name;
        this.serviceTimeGenerator = serviceTimeGenerator;
    }

    public String getName() {
        return name;
    }

    public boolean isBusy() {
        return busy;
    }

    public void addCustomer(Customer c) {
        queue.add(c);
    }

    public Customer getNextCustomer() {
        return queue.poll();
    }

    public boolean hasWaitingCustomer() {
        return !queue.isEmpty();
    }

    public double generateServiceTime() {
        return serviceTimeGenerator.sample();
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }

    public int getQueueLength() {
        return queue.size();
    }
}
