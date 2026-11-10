package rentgo.exception;

/** Signals that a vehicle cannot be rented in its current state. */
public class VehicleUnavailableException extends RuntimeException {
    public VehicleUnavailableException(String message) { super(message); }
    public VehicleUnavailableException(String message, Throwable cause) { super(message, cause); }
}