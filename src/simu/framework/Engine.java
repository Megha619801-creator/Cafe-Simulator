package simu.framework;

import simulation.ui.IControllerMtoV;
import simu.model.ServicePoint;

public abstract class Engine extends Thread implements IEngine { // NEW DEFINITIONS
	private double simulationTime = 0; // time when the simulation will be stopped
	private long delay = 0;
	private Clock clock; // in order to simplify the code (clock.getClock() instead
							// Clock.getInstance().getClock())

	protected EventList eventList;
	protected ServicePoint[] servicePoints;
	protected IControllerMtoV controller; // NEW
	private final Object pauseLock = new Object();
	private boolean paused = false;
	private boolean stepMode = false;
	private boolean stepRequested = false;

	public Engine(IControllerMtoV controller) { // NEW
		this.controller = controller; // NEW
		clock = Clock.getInstance();
		eventList = new EventList();
		/*
		 * Service Points are created in simu.model-package's class who is inheriting
		 * the Engine class
		 */
	}

	@Override
	public void setSimulationTime(double time) {
		simulationTime = time;
	}

	@Override // NEW
	public void setDelay(long time) {
		this.delay = time;
	}

	@Override // NEW
	public long getDelay() {
		return delay;
	}

	@Override
	public void run() {
		initialization(); // creating, e.g., the first event

		while (simulate()) {
			waitIfPaused();
			delay(); // NEW
			clock.setTime(currentTime());
			runBEvents();
			tryCEvents();
		}

		results();
	}

	private void waitIfPaused() {
		synchronized (pauseLock) {
			while ((paused || stepMode) && !stepRequested) {
				try {
					pauseLock.wait();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return;
				}
			}
			if (stepRequested) {
				stepRequested = false;
			}
		}
	}

	private void runBEvents() {
		while (eventList.getNextTime() == clock.getTime()) {
			runEvent(eventList.remove());
		}
	}

	private void tryCEvents() { // define protected, if you want to overwrite
		for (ServicePoint p : servicePoints) {
			if (!p.isReserved() && p.isOnQueue()) {
				p.beginService();
			}
		}
	}

	private double currentTime() {
		return eventList.getNextTime();
	}

	private boolean simulate() {
		Trace.out(Trace.Level.INFO, "Time is: " + clock.getTime());
		return clock.getTime() < simulationTime;
	}

	private void delay() { // NEW
		Trace.out(Trace.Level.INFO, "Delay " + delay);
		try {
			sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void pauseSimulation() {
		synchronized (pauseLock) {
			paused = true;
			pauseLock.notifyAll();
		}
	}

	@Override
	public void resumeSimulation() {
		synchronized (pauseLock) {
			paused = false;
			stepMode = false;
			stepRequested = false;
			pauseLock.notifyAll();
		}
	}

	@Override
	public void stepOnce() {
		synchronized (pauseLock) {
			paused = true;
			stepMode = true;
			stepRequested = true;
			pauseLock.notifyAll();
		}
	}

	@Override
	public boolean isPaused() {
		synchronized (pauseLock) {
			return paused || stepMode;
		}
	}

	protected abstract void initialization(); // Defined in simu.model-package's class who is inheriting the Engine
												// class

	protected abstract void runEvent(Event t); // Defined in simu.model-package's class who is inheriting the Engine
												// class

	protected abstract void results(); // Defined in simu.model-package's class who is inheriting the Engine class
}