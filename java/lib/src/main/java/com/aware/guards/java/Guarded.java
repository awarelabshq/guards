package com.aware.guards.java;


import io.opentelemetry.api.trace.SpanKind;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Guarded {

    // Enable the guard after this date (Formatted as YYYY-MM-DD)
    String since() default "";

    // Comma separated list of conditions to verify eg: "avg_latency < 500"
    String condition() default "";

    // Severity of the guard. If FATAL, break the CI / CD pipeline.
    Severity severity() default Severity.WARN;

    // The environment to use for validation of the conditions. If not specified, the default test environment
    // configured in Project Settings in Aware will be used.
    String environment() default "";

    // Additional filters to apply for evaluation scoping. For instance, if there is an attribute "input_size" in the
    // current span, and you want to evaluate the conditions scoped for inputs greater than 1000,
    // set filters = "input_size > 1000"
    String filters() default "";

    // The name of the span to be created for the guarded function. If not specified, ClassName.functionName will be
    // used (same as Otel span naming logic).
    String value() default "";

    // If set to false, the guard metadata is added to the existing current span instead of creating a new span.
    boolean createNewSpan() default true;

    // The type of OpenTelemetry Span created.
    SpanKind kind() default SpanKind.INTERNAL;
}
