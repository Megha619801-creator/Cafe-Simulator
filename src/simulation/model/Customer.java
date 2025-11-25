package simulation.model;

public class Customer {
    private static int counter = 0;
    private final int id;
    private final String type; // "INSTORE" or "MOBILE"
    private double arrivalTime;
    private double serviceStartTime;
    private double serviceEndTime;

    public Customer(String type, double arrivalTime) {
        this.id = ++counter;
        this.type = type;
        this.arrivalTime = arrivalTime;
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public double getArrivalTime() {
        return arrivalTime;
    }

    public void setServiceStartTime(double time) {
        this.serviceStartTime = time;
    }

    public void setServiceEndTime(double time) {
        this.serviceEndTime = time;
    }

    public double getServiceTime() {
        return serviceEndTime - serviceStartTime;
    }

    public double getWaitingTime() {
        return serviceStartTime - arrivalTime;
    }

    public double getResponseTime() {
        return serviceEndTime - arrivalTime;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", type='" + type + '\'' +
                '}';
    }
}
