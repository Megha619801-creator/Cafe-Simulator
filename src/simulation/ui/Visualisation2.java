package simulation.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import simulation.model.Customer;

import java.util.HashMap;
import java.util.Map;

public class Visualisation2 extends Canvas implements IVisualisation {
    private final GraphicsContext gc;
    private final Map<Customer, Integer> customerPositions = new HashMap<>();
    private final int[] spYPositions = new int[4];

    public Visualisation2(int w, int h) {
        super(w, h);
        gc = this.getGraphicsContext2D();
        clearDisplay();
    }

    @Override
    public void clearDisplay() {
        gc.setFill(Color.YELLOW);
        gc.fillRect(0, 0, getWidth(), getHeight());
        for (int i = 0; i < spYPositions.length; i++) {
            spYPositions[i] = 20;
        }
        customerPositions.clear();
    }

    @Override
    public void addCustomer(Customer c, int servicePointIndex) {
        int x = 50 + servicePointIndex * 100;
        int y = spYPositions[servicePointIndex];
        drawCustomer(c, x, y);
        customerPositions.put(c, servicePointIndex);
        spYPositions[servicePointIndex] += 20;
    }

    @Override
    public void moveCustomer(Customer c, int servicePointIndex) {
        if (!customerPositions.containsKey(c)) {
            addCustomer(c, servicePointIndex);
            return;
        }
        customerPositions.put(c, servicePointIndex);
        redrawAll();
    }

    @Override
    public void removeCustomer(Customer c) {
        customerPositions.remove(c);
        redrawAll();
    }

    private void redrawAll() {
        clearDisplay();
        for (Map.Entry<Customer, Integer> entry : customerPositions.entrySet()) {
            int spIndex = entry.getValue();
            int x = 50 + spIndex * 100;
            int y = spYPositions[spIndex];
            drawCustomer(entry.getKey(), x, y);
            spYPositions[spIndex] += 20;
        }
    }

    private void drawCustomer(Customer customer, int x, int y) {
        gc.setFill(Color.RED);
        gc.fillOval(x, y, 15, 15);
        gc.setFill(Color.BLACK);
        gc.setFont(new Font(12));
        gc.fillText("C" + customer.getId(), x, y - 2);
    }
}
