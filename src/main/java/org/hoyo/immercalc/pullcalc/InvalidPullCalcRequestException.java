package org.hoyo.immercalc.pullcalc;

public class InvalidPullCalcRequestException extends RuntimeException {
    public InvalidPullCalcRequestException(String message) {
        super(message);
    }
}
