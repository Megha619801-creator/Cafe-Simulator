package simu.framework;

/* Controller uses this interface */
public interface IEngine { // NEW
	void setSimulationTime(double time);

	void setDelay(long time);

	long getDelay();

	void pauseSimulation();

	void resumeSimulation();

	void stepOnce();

	boolean isPaused();
}
