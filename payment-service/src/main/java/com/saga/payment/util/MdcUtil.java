package com.saga.payment.util;

import org.slf4j.MDC;

public final class MdcUtil {

    private MdcUtil() {
    }

    public static void putIfNotNull(String key, String value) {
        if (value != null) {
            MDC.put(key, value);
        }
    }

    public static void putTraceAndEventId(String traceId, String eventId) {
        putIfNotNull("traceId", traceId);
        putIfNotNull("eventId", eventId);
    }

    public static void clear() {
        MDC.clear();
    }
}