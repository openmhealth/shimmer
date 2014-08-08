package org.openmhealth.shim;

/**
 * Base class for all shim related exceptions.
 */
public class ShimException extends Exception {

    public ShimException() {
    }

    public ShimException(String s) {
        super(s);
    }

    public ShimException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
