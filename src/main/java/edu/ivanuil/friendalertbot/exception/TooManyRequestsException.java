package edu.ivanuil.friendalertbot.exception;

public class TooManyRequestsException extends RuntimeException {

    public TooManyRequestsException(final Throwable cause) {
        super(cause);
    }

}
