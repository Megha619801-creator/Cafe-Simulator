package simulation.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Window;
import javafx.util.Duration;
import simulation.model.Customer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Draws a clean horizontal flow between service points using custom icons and
 * capsules for customers.
 */
public class Visualisation extends Canvas implements IVisualisation {

    private static final double HORIZONTAL_PADDING = 80;
    private static final double ICON_Y = 70;
    private static final double QUEUE_SPACING = 24;
    private static final double LEGEND_PADDING = 16;
    private static final double LEGEND_WIDTH = 150;
    private static final double LEGEND_HEIGHT = 70;
    private static final double ICON_HIT_RADIUS = 45;
    private static final double ICON_SIZE = 70;

    private static final double CUSTOMER_WIDTH = 34;
    private static final double CUSTOMER_HEIGHT = 18;

    private static final Color BACKGROUND = Color.web("#FFF5E9");
    private static final Color GUIDE_COLOR = Color.web("#BCAAA4");
    private static final Color TEXT_COLOR = Color.web("#2B2118");
    private static final Color INSTORE_COLOR = Color.web("#4A63D8");
    private static final Color MOBILE_COLOR = Color.web("#00897B");
    private static final Color LEGEND_BACKGROUND = Color.web("#FFFFFF", 0.95);
    private static final Color LEGEND_BORDER = Color.web("#8D6E63");

    private final GraphicsContext gc;
    private final Map<Customer, Integer> customerLaneIndex = new HashMap<>();
    private final List<List<Customer>> laneQueues = new ArrayList<>();
    private final Tooltip laneTooltip;
    private ServiceLane highlightedLane;
    private final Image cashierIcon;
    private final Image baristaIcon;
    private final Image finishingIcon;
    private final Image pickupIcon;

    public Visualisation(int w, int h) {
        super(Math.max(w, 680), Math.max(h, 200));
        gc = getGraphicsContext2D();
        for (int i = 0; i < ServiceLane.values().length; i++) {
            laneQueues.add(new ArrayList<>());
        }
        laneTooltip = new Tooltip();
        laneTooltip.setShowDelay(Duration.millis(120));
        laneTooltip.setHideDelay(Duration.millis(80));
        laneTooltip.setAutoHide(true);

        cashierIcon = loadIcon("/icons/cashier.png");
        baristaIcon = loadIcon("/icons/barista.png");
        finishingIcon = loadIcon("/icons/prepare.png");
        pickupIcon = loadIcon("/icons/ready.png");

        setOnMouseMoved(this::handleMouseMoved);
        setOnMouseExited(event -> {
            highlightedLane = null;
            laneTooltip.hide();
        });
        clearDisplay();
    }

    @Override
    public void clearDisplay() {
        customerLaneIndex.clear();
        laneQueues.forEach(List::clear);
        drawScene();
    }

    @Override
    public void addCustomer(Customer c, int servicePointIndex) {
        moveToLane(c, servicePointIndex);
    }

    @Override
    public void moveCustomer(Customer c, int servicePointIndex) {
        moveToLane(c, servicePointIndex);
    }

    @Override
    public void removeCustomer(Customer c) {
        Integer lane = customerLaneIndex.remove(c);
        if (lane != null) {
            laneQueues.get(lane).remove(c);
        }
        drawScene();
    }

    private void moveToLane(Customer c, int laneIndex) {
        Integer current = customerLaneIndex.put(c, laneIndex);
        if (current != null) {
            laneQueues.get(current).remove(c);
        }
        laneQueues.get(laneIndex).add(c);
        drawScene();
    }

    private void drawScene() {
        drawBackground();
        drawFlowGuides();
        drawServicePoints();
        drawCustomers();
        drawLegend();
    }

    private void drawBackground() {
        gc.setFill(BACKGROUND);
        gc.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawFlowGuides() {
        gc.setStroke(GUIDE_COLOR);
        gc.setLineWidth(2);
        double guideBottom = getHeight() - 60;
        for (int i = 0; i < ServiceLane.values().length; i++) {
            double x = laneX(i);
            gc.strokeLine(x, ICON_Y + 30, x, guideBottom);
        }
        double arrowY = guideBottom + 16;
        gc.setLineDashes(8);
        for (int i = 0; i < ServiceLane.values().length - 1; i++) {
            double from = laneX(i);
            double to = laneX(i + 1);
            gc.strokeLine(from, arrowY, to, arrowY);
            drawArrowHead(to, arrowY);
        }
        gc.setLineDashes(0);
    }

    private void drawArrowHead(double x, double y) {
        gc.strokeLine(x, y, x - 8, y - 4);
        gc.strokeLine(x, y, x - 8, y + 4);
    }

    private void drawServicePoints() {
        for (int i = 0; i < ServiceLane.values().length; i++) {
            double x = laneX(i);
            drawServiceIcon(ServiceLane.values()[i], x, ICON_Y);
            gc.setFill(TEXT_COLOR);
            gc.setFont(Font.font("Inter", javafx.scene.text.FontWeight.SEMI_BOLD, 16));
            gc.fillText(ServiceLane.values()[i].label, x - 35, ICON_Y + 70);
        }
    }

    private void drawServiceIcon(ServiceLane lane, double cx, double cy) {
        Image icon = switch (lane) {
            case CASHIER -> cashierIcon;
            case BARISTA -> baristaIcon;
            case FINISHING -> finishingIcon;
            case PICKUP -> pickupIcon;
        };
        if (icon != null) {
            double x = cx - ICON_SIZE / 2;
            double y = cy - ICON_SIZE / 2;
            gc.drawImage(icon, x, y, ICON_SIZE, ICON_SIZE);
        } else {
            gc.setStroke(TEXT_COLOR);
            gc.setLineWidth(1.8);
            switch (lane) {
                case CASHIER -> drawFallbackCashier(cx, cy);
                case BARISTA -> drawFallbackBarista(cx, cy);
                case FINISHING -> drawFallbackFinishing(cx, cy);
                case PICKUP -> drawFallbackPickup(cx, cy);
            }
        }
    }

    private void drawFallbackCashier(double cx, double cy) {
        gc.setFill(Color.web("#FFCC80"));
        gc.fillOval(cx - 14, cy - 32, 28, 28);
        gc.setStroke(TEXT_COLOR);
        gc.strokeOval(cx - 14, cy - 32, 28, 28);
        gc.setFill(Color.web("#8D6E63"));
        gc.fillRoundRect(cx - 12, cy - 10, 24, 26, 8, 8);
        gc.setStroke(TEXT_COLOR);
        gc.strokeLine(cx - 22, cy + 14, cx + 22, cy + 14);
    }

    private void drawFallbackBarista(double cx, double cy) {
        gc.setFill(Color.web("#C5E1A5"));
        gc.fillRoundRect(cx - 22, cy - 22, 44, 40, 12, 12);
        gc.setStroke(TEXT_COLOR);
        gc.strokeRoundRect(cx - 22, cy - 22, 44, 40, 12, 12);
        gc.setFill(Color.web("#AED581"));
        gc.fillRect(cx - 14, cy - 2, 28, 24);
        gc.setStroke(TEXT_COLOR);
        gc.strokeLine(cx, cy - 12, cx, cy + 18);
    }

    private void drawFallbackPickup(double cx, double cy) {
        double[] xPoints = { cx - 20, cx + 20, cx + 14, cx - 14 };
        double[] yPoints = { cy - 12, cy - 12, cy + 32, cy + 32 };
        gc.setFill(Color.web("#FFE0B2"));
        gc.fillPolygon(xPoints, yPoints, 4);
        gc.setStroke(TEXT_COLOR);
        gc.strokePolygon(xPoints, yPoints, 4);
        gc.strokeLine(cx - 12, cy - 16, cx + 12, cy - 16);
    }

    private void drawFallbackFinishing(double cx, double cy) {
        gc.setFill(Color.web("#FFE082"));
        gc.fillRoundRect(cx - 18, cy - 24, 36, 32, 8, 8);
        gc.setStroke(TEXT_COLOR);
        gc.strokeRoundRect(cx - 18, cy - 24, 36, 32, 8, 8);
        gc.setFill(Color.web("#FFECB3"));
        gc.fillRect(cx - 14, cy - 4, 28, 18);
        gc.setStroke(TEXT_COLOR);
        gc.strokeLine(cx - 14, cy + 6, cx + 14, cy + 6);
        for (int i = -2; i <= 2; i += 2) {
            gc.strokeLine(cx + i * 4, cy - 14, cx + i * 4, cy - 6);
        }
    }

    private void drawCustomers() {
        for (int laneIndex = 0; laneIndex < laneQueues.size(); laneIndex++) {
            List<Customer> queue = laneQueues.get(laneIndex);
            double baseX = laneX(laneIndex);
            double laneBaseline = ICON_Y + 100;
            for (int position = 0; position < queue.size(); position++) {
                Customer customer = queue.get(position);
                double y = laneBaseline + position * QUEUE_SPACING;
                drawCustomerSprite(customer, baseX, y);
            }
        }
    }

    private void drawCustomerSprite(Customer customer, double centerX, double centerY) {
        boolean instore = "INSTORE".equalsIgnoreCase(customer.getType());
        Color fill = instore ? INSTORE_COLOR : MOBILE_COLOR;
        String badge = instore ? "IN" : "MB";

        double x = centerX - CUSTOMER_WIDTH / 2.0;
        double y = centerY - CUSTOMER_HEIGHT / 2.0;

        gc.setFill(fill);
        gc.fillRoundRect(x, y, CUSTOMER_WIDTH, CUSTOMER_HEIGHT, CUSTOMER_HEIGHT, CUSTOMER_HEIGHT);
        gc.setStroke(Color.WHITE);
        gc.strokeRoundRect(x, y, CUSTOMER_WIDTH, CUSTOMER_HEIGHT, CUSTOMER_HEIGHT, CUSTOMER_HEIGHT);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Inter", 10));
        gc.fillText(badge, centerX - 11, centerY + 3);
    }

    private void drawLegend() {
        double boxX = getWidth() - LEGEND_WIDTH - LEGEND_PADDING;
        double boxY = getHeight() - LEGEND_HEIGHT - LEGEND_PADDING;

        gc.setFill(LEGEND_BACKGROUND);
        gc.fillRoundRect(boxX, boxY, LEGEND_WIDTH, LEGEND_HEIGHT, 12, 12);
        gc.setStroke(LEGEND_BORDER);
        gc.setLineWidth(1);
        gc.strokeRoundRect(boxX, boxY, LEGEND_WIDTH, LEGEND_HEIGHT, 12, 12);

        gc.setFill(TEXT_COLOR);
        gc.setFont(Font.font("Inter", 12));
        gc.fillText("Legend", boxX + 12, boxY + 18);

        drawLegendEntry(boxX + 12, boxY + 30, INSTORE_COLOR, "IN = In-store order");
        drawLegendEntry(boxX + 12, boxY + 50, MOBILE_COLOR, "MB = Mobile order");
    }

    private void drawLegendEntry(double x, double y, Color color, String text) {
        gc.setFill(color);
        gc.fillRoundRect(x, y - 10, 22, 14, 7, 7);
        gc.setStroke(Color.WHITE);
        gc.strokeRoundRect(x, y - 10, 22, 14, 7, 7);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Inter", 9));
        gc.fillText(text.startsWith("IN") ? "IN" : "MB", x + 4, y);

        gc.setFill(TEXT_COLOR);
        gc.setFont(Font.font("Inter", 11));
        gc.fillText(text, x + 32, y + 1);
    }

    private void handleMouseMoved(MouseEvent event) {
        ServiceLane lane = findLaneAt(event.getX(), event.getY());
        if (lane == null) {
            highlightedLane = null;
            laneTooltip.hide();
            return;
        }
        if (lane == highlightedLane && laneTooltip.isShowing()) {
            return;
        }
        highlightedLane = lane;
        laneTooltip.setText(lane.description);
        Window window = getScene() != null ? getScene().getWindow() : null;
        if (window == null) {
            return;
        }
        laneTooltip.show(this, event.getScreenX() + 12, event.getScreenY() + 8);
    }

    private ServiceLane findLaneAt(double mouseX, double mouseY) {
        for (int i = 0; i < ServiceLane.values().length; i++) {
            double laneCenterX = laneX(i);
            double laneCenterY = ICON_Y;
            double dx = mouseX - laneCenterX;
            double dy = mouseY - laneCenterY;
            if (Math.hypot(dx, dy) <= ICON_HIT_RADIUS) {
                return ServiceLane.values()[i];
            }
        }
        return null;
    }

    private enum ServiceLane {
        CASHIER("Cashier", "Cashier – greets customers, takes new orders, and handles payment."),
        BARISTA("Barista", "Barista – prepares espresso shots and steams milk for every drink."),
        FINISHING("Finishing", "Finishing – adds toppings, checks accuracy, and bags food items."),
        PICKUP("Pickup", "Pickup – calls out names and hands drinks to waiting guests.");

        private final String label;
        private final String description;

        ServiceLane(String label, String description) {
            this.label = label;
            this.description = description;
        }
    }

    private double laneX(int index) {
        int laneCount = ServiceLane.values().length;
        if (laneCount == 1) {
            return getWidth() / 2.0;
        }
        double available = Math.max(1, getWidth() - 2 * HORIZONTAL_PADDING);
        double step = available / (laneCount - 1);
        return HORIZONTAL_PADDING + index * step;
    }

    public void resizeCanvas(double width, double height) {
        double newWidth = Math.max(1, width);
        double newHeight = Math.max(1, height);
        boolean changed = false;
        if (newWidth != getWidth()) {
            setWidth(newWidth);
            changed = true;
        }
        if (newHeight != getHeight()) {
            setHeight(newHeight);
            changed = true;
        }
        if (changed) {
            drawScene();
        }
    }

    private Image loadIcon(String resourcePath) {
        try {
            return new Image(Objects.requireNonNull(getClass().getResourceAsStream(resourcePath)));
        } catch (Exception e) {
            return null;
        }
    }
}
