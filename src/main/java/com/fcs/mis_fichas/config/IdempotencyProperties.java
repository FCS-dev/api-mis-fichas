package com.fcs.mis_fichas.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "idempotency")
public class IdempotencyProperties {

    private Duration ttl = Duration.ofHours(24);
    private int maxEntries = 1000;
}
