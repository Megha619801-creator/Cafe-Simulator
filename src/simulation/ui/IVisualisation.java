package simulation.ui;

import simulation.model.Customer;

public interface IVisualisation {
    void clearDisplay();

    void addCustomer(Customer customer, int servicePointIndex);

    void moveCustomer(Customer customer, int servicePointIndex);

    void removeCustomer(Customer customer);
}
