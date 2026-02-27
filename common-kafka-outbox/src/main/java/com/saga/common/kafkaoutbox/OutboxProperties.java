package com.saga.common.kafkaoutbox;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "outbox")
public record OutboxProperties(Batch batch, Retry retry, Inflight inflight, Lease lease) {

    public OutboxProperties {
        if (batch == null) {
            batch = new Batch(100);
        }
        Retry.Interval interval = retry != null && retry.interval() != null ? retry.interval() : new Retry.Interval(5);
        Retry.Max max = retry != null && retry.max() != null ? retry.max() : new Retry.Max(10);
        retry = new Retry(interval, max);
        if (inflight == null) {
            inflight = new Inflight(100);
        }
        if (lease == null) {
            lease = new Lease(180);
        }
    }

    public record Batch(int size) {}

    public record Inflight(int limit) {}

    public record Lease(long seconds) {}

    public record Retry(Interval interval, Max max) {
        public record Interval(long seconds) {}
        public record Max(int attempts) {}
    }
}
