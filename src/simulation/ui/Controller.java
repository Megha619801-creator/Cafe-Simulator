package simulation.ui;

import javafx.application.Platform;
import simu.framework.Clock;
import simu.framework.IEngine;
import simulation.model.Customer;
import simu.model.MyEngine;

public class Controller implements IControllerVtoM, IControllerMtoV {
    private IEngine engine;
    private final ISimulatorUI ui;

    public Controller(ISimulatorUI ui) {
        this.ui = ui;
    }

    @Override
    public void startSimulation() {
        if (engine != null) {
            return;
        }
        Clock.getInstance().setTime(0); // ensure every run starts from time zero
        engine = new MyEngine(this);
        engine.setSimulationTime(ui.getTime());
        engine.setDelay(ui.getDelay());
        ui.showCurrentDelay(engine.getDelay());
        ui.getVisualisation().clearDisplay();
        ((Thread) engine).start();
    }

    @Override
    public void decreaseSpeed() {
        if (engine == null) {
            return;
        }
        long updatedDelay = Math.max(1, (long) (engine.getDelay() * 1.10));
        engine.setDelay(updatedDelay);
        ui.showCurrentDelay(updatedDelay);
    }

    @Override
    public void increaseSpeed() {
        if (engine == null) {
            return;
        }
        long updatedDelay = Math.max(1, (long) (engine.getDelay() * 0.9));
        engine.setDelay(updatedDelay);
        ui.showCurrentDelay(updatedDelay);
    }

    @Override
    public void showEndTime(double time) {
        Platform.runLater(() -> ui.setEndingTime(time));
        engine = null;
    }

    @Override
    public void visualiseCustomer(Customer c, int servicePointIndex) {
        Platform.runLater(() -> ui.getVisualisation().moveCustomer(c, servicePointIndex));
    }

    @Override
    public void removeCustomer(Customer c) {
        Platform.runLater(() -> ui.getVisualisation().removeCustomer(c));
    }

    @Override
    public void pauseSimulation() {
        if (engine != null) {
            engine.pauseSimulation();
        }
    }

    @Override
    public void resumeSimulation() {
        if (engine != null) {
            engine.resumeSimulation();
        }
    }

    @Override
    public void stepSimulation() {
        if (engine != null) {
            engine.stepOnce();
        }
    }

    @Override
    public boolean isSimulationRunning() {
        return engine != null;
    }

    @Override
    public void setDelay(long delay) {
        if (engine == null) {
            return;
        }
        engine.setDelay(Math.max(1, delay));
        ui.showCurrentDelay(engine.getDelay());
    }
}
