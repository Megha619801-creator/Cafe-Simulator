package simu.model;

import simu.framework.Clock;
import simu.framework.Trace;

public class Customer {
    private double arrivalTime;
    private double removalTime;
    private int id;
    private CustomerType type;
    private static int i = 1;
    private static long sum = 0;

    public Customer(CustomerType type) {
        this.id = i++;
        this.type = type;

        this.arrivalTime = Clock.getInstance().getTime();
        Trace.out(Trace.Level.INFO, "New " + type + " customer #" + id + " arrived at " + arrivalTime);
    }

    public double getRemovalTime() {
        return removalTime;
    }

    public void setRemovalTime(double removalTime) {
        this.removalTime = removalTime;
    }

    public double getArrivalTime() {
        return arrivalTime;
    }

    public CustomerType getType() {
        return type;
    }

    public void reportResults() {
        Trace.out(Trace.Level.INFO, "\nCustomer " + id + " (" + type + ") ready!");
        Trace.out(Trace.Level.INFO, "Customer " + id + " arrived: " + arrivalTime);
        Trace.out(Trace.Level.INFO, "Customer " + id + " removed: " + removalTime);
        Trace.out(Trace.Level.INFO, "Customer " + id + " stayed: " + (removalTime - arrivalTime));

        sum += (removalTime - arrivalTime);
        double mean = sum / id;
        System.out.println("Current mean of the customer service times: " + mean);
    }
}
