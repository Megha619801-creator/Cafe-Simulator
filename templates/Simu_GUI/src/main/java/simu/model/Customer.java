package simu.model;

import simu.framework.Clock;
import simu.framework.Trace;

public class Customer {
    private double arrivalTime;
    private double removalTime;
    private int id;
    private CustomerType type;
    private static int i = 1;
    private static double sum = 0;  // changed to double
    private static int finishedCustomers = 0;  // track finished customers

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
        double serviceTime = removalTime - arrivalTime;
        if (serviceTime < 0) serviceTime = 0;  // safeguard against negative time

        finishedCustomers++;
        sum += serviceTime;
        double mean = sum / finishedCustomers;

        Trace.out(Trace.Level.INFO, "\nCustomer " + id + " (" + type + ") ready!");
        Trace.out(Trace.Level.INFO, "Customer " + id + " arrived: " + arrivalTime);
        Trace.out(Trace.Level.INFO, "Customer " + id + " removed: " + removalTime);
        Trace.out(Trace.Level.INFO, "Customer " + id + " stayed: " + serviceTime);

        System.out.println("Current mean of the customer service times: " + mean);
    }
}
