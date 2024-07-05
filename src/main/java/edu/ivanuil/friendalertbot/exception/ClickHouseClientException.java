package edu.ivanuil.friendalertbot.exception;

public class ClickHouseClientException extends RuntimeException {

    public ClickHouseClientException() {
        super();
    }

    public ClickHouseClientException(final String message) {
        super(message);
    }

    public ClickHouseClientException(final Throwable cause) {
        super(cause);
    }

}
