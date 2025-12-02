package simulation.ui;

public interface IControllerVtoM {
    void startSimulation();

    void increaseSpeed();

    void decreaseSpeed();

    void pauseSimulation();

    void resumeSimulation();

    void stepSimulation();

    boolean isSimulationRunning();

    void setDelay(long delay);
}
