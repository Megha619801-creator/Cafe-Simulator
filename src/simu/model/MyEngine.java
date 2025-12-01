package simu.model;

import eduni.distributions.Negexp;
import eduni.distributions.Normal;
import simulation.model.Customer;
import simulation.ui.IControllerMtoV;
import simu.framework.ArrivalProcess;
import simu.framework.Clock;
import simu.framework.Engine;
import simu.framework.Event;

public class MyEngine extends Engine {
    private static final String INSTORE = "INSTORE";
    private static final String MOBILE = "MOBILE";

    private ArrivalProcess instoreArrival;
    private ArrivalProcess mobileArrival;

    public MyEngine(IControllerMtoV controller) {
        super(controller);

        servicePoints = new ServicePoint[4];

        // 4 service points
        servicePoints[0] = new ServicePoint(new Normal(3, 1), eventList, EventType.DEP_CASHIER);
        servicePoints[1] = new ServicePoint(new Normal(5, 2), eventList, EventType.DEP_BARISTA1);
        servicePoints[2] = new ServicePoint(new Normal(4, 1.5), eventList, EventType.DEP_BARISTA2);
        servicePoints[3] = new ServicePoint(new Normal(2, 1), eventList, EventType.DEP_PICKUP);

        // Arrival processes
        instoreArrival = new ArrivalProcess(new Negexp(10, 2), eventList, EventType.ARRIVAL_INSTORE);
        mobileArrival = new ArrivalProcess(new Negexp(12, 3), eventList, EventType.ARRIVAL_MOBILE);
    }

    @Override
    protected void initialization() {
        instoreArrival.generateNext();
        mobileArrival.generateNext();
    }

    @Override
    protected void runEvent(Event t) {
        Customer c;

        switch ((EventType) t.getType()) {
            case ARRIVAL_INSTORE:
                c = new Customer(INSTORE, Clock.getInstance().getTime());
                servicePoints[0].addQueue(c);
                instoreArrival.generateNext();
                controller.visualiseCustomer(c, 0); // Cashier
                break;

            case ARRIVAL_MOBILE:
                c = new Customer(MOBILE, Clock.getInstance().getTime());
                servicePoints[1].addQueue(c); // skips cashier
                mobileArrival.generateNext();
                controller.visualiseCustomer(c, 1); // Barista1
                break;

            case DEP_CASHIER:
                c = servicePoints[0].removeQueue();
                servicePoints[1].addQueue(c);
                controller.visualiseCustomer(c, 1); // Barista1
                break;

            case DEP_BARISTA1:
                c = servicePoints[1].removeQueue();
                if (INSTORE.equals(c.getType())) {
                    servicePoints[2].addQueue(c);
                    controller.visualiseCustomer(c, 2); // Barista2
                } else {
                    servicePoints[3].addQueue(c);
                    controller.visualiseCustomer(c, 3); // Pickup
                }
                break;

            case DEP_BARISTA2:
                c = servicePoints[2].removeQueue();
                servicePoints[3].addQueue(c);
                controller.visualiseCustomer(c, 3); // Pickup
                break;

            case DEP_PICKUP:
                c = servicePoints[3].removeQueue();
                controller.removeCustomer(c);
                break;
        }
    }

    @Override
    protected void results() {
        controller.showEndTime(Clock.getInstance().getTime());
    }
}
