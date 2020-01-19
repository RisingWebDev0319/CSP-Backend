package ca.freshstart.data.concreteEvent.types;

/**
 * cancelled <- tentative <- -> confirmed -> completed
 */
public enum ConcreteEventState {
    tentative, // The event is not confirmed. Occurs after any editing
    confirmed, // Confirmed by the therapist
    cancelled, // Cancelled by the therapist
    completed
}
