package simu.model;

import simu.framework.IEventType;

// Event types for 4 service points and arrival
public enum EventType implements IEventType {
    ARRIVAL_INSTORE, ARRIVAL_MOBILE,
    DEP_CASHIER, DEP_BARISTA1, DEP_BARISTA2, DEP_PICKUP
}
