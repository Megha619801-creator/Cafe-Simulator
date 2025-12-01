package simulation.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import simulation.model.Customer;

import java.util.HashMap;

public class Visualisation extends Canvas implements IVisualisation {
    private final GraphicsContext gc;
    private final double[] xPositions = { 50, 150, 250, 350 };
    private final double yBase = 50;
    private final HashMap<Customer, double[]> customerPositions = new HashMap<>();

    public Visualisation(int w, int h) {
        super(w, h);
        gc = this.getGraphicsContext2D();
        clearDisplay();
    }

    @Override
    public void clearDisplay() {
        gc.setFill(Color.LIGHTYELLOW);
        gc.fillRect(0, 0, getWidth(), getHeight());
        customerPositions.clear();
    }

    @Override
    public void addCustomer(Customer c, int servicePointIndex) {
        double x = xPositions[servicePointIndex];
        double y = yBase + customerPositions.size() * 15;
        customerPositions.put(c, new double[] { x, y });
        drawCustomers();
    }

    @Override
    public void moveCustomer(Customer c, int servicePointIndex) {
        if (customerPositions.containsKey(c)) {
            double x = xPositions[servicePointIndex];
            double y = customerPositions.get(c)[1];
            customerPositions.put(c, new double[] { x, y });
            drawCustomers();
        } else {
            addCustomer(c, servicePointIndex);
        }
    }

    @Override
    public void removeCustomer(Customer c) {
        customerPositions.remove(c);
        drawCustomers();
    }

    private void drawCustomers() {
        gc.setFill(Color.LIGHTYELLOW);
        gc.fillRect(0, 0, getWidth(), getHeight());

        gc.setFill(Color.RED);
        for (double[] pos : customerPositions.values()) {
            gc.fillOval(pos[0], pos[1], 12, 12);
        }
    }
}
