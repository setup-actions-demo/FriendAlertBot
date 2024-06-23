package edu.ivanuil.friendalertbot.exception;

public class TooManyRequestsException extends RuntimeException {

    public TooManyRequestsException(Throwable cause) {
        super(cause);
    }

}
