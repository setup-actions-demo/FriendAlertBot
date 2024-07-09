package edu.ivanuil.friendalertbot.util;

import java.sql.Timestamp;

public class RequestRateUtil {

    private int requestCount = 0;
    private Timestamp startTime = new Timestamp(System.currentTimeMillis());

    public synchronized void incrementRequestCount() {
        requestCount++;
    }

    public double getRatePerSecond() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        double seconds = (now.getTime() - startTime.getTime()) / 1000.0;
        return requestCount / seconds;
    }

    public synchronized double getRatePerSecondAndReset() {
        double rate = getRatePerSecond();
        requestCount = 0;
        startTime = new Timestamp(System.currentTimeMillis());
        return rate;
    }

}
