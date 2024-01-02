package com.aware.guards.java;

import io.opentelemetry.api.OpenTelemetry;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConditionalOnClass(Aspect.class)
@ConditionalOnProperty(name = "otel.instrumentation.annotations.enabled", matchIfMissing = true)
@Configuration
public class GuardedConfiguration {

    @Bean
    GuardedAspect guardedAspect(OpenTelemetry openTelemetry) {
        return new GuardedAspect(openTelemetry);
    }
    
}
