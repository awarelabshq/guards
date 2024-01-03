package com.aware.guards.java;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;
import java.time.Instant;

import static com.aware.guards.java.Constants.*;


@Aspect
@Order(Ordered.LOWEST_PRECEDENCE)
public class GuardedAspect {

    private final OpenTelemetry openTelemetry;

    public GuardedAspect(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
    }

    @Around("@annotation(com.aware.guards.java.Guarded)")
    public Object traceMethod(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        Guarded guarded = method.getAnnotation(Guarded.class);

        Span span;
        if (method.isAnnotationPresent(WithSpan.class) || !guarded.createNewSpan()) {
            span = Span.current();
            span.setAttribute(AWARE_GUARD_ATTRIB_SINCE, guarded.since())
                    .setAttribute(AWARE_GUARD_ATTRIB_CONDITION, guarded.condition())
                    .setAttribute(AWARE_GUARD_ATTRIB_ENVIRONMENT, guarded.environment())
                    .setAttribute(AWARE_GUARD_ATTRIB_FILTERS, guarded.filters())
                    .setAttribute(AWARE_GUARD_ATTRIB_SEVERITY, guarded.severity().name());
            return pjp.proceed();
        } else {
            Context parent = Context.current();
            Tracer tracer = openTelemetry.getTracer(GuardedAspect.class.getName());
            String spanName = getSpanName(guarded, method);
            span = tracer.spanBuilder(spanName).setParent(parent).setSpanKind(guarded.kind())
                    .setStartTimestamp(Instant.now())
                    .setAttribute(AWARE_GUARD_ATTRIB_SINCE, guarded.since())
                    .setAttribute(AWARE_GUARD_ATTRIB_CONDITION, guarded.condition())
                    .setAttribute(AWARE_GUARD_ATTRIB_ENVIRONMENT, guarded.environment())
                    .setAttribute(AWARE_GUARD_ATTRIB_FILTERS, guarded.filters())
                    .setAttribute(AWARE_GUARD_ATTRIB_SEVERITY, guarded.severity().name()).startSpan();
            try (Scope ignored = span.makeCurrent()) {
                return pjp.proceed();
            } catch (Throwable t) {
                span.setStatus(StatusCode.ERROR);
                span.recordException(t);
                throw t;
            } finally {
                span.end();
            }
        }

    }

    private String getSpanName(Guarded guarded, Method method) {
        String spanName = guarded.value();
        if (spanName.isEmpty()) {
            return method.getDeclaringClass().getSimpleName() + "." + method.getName();
        }
        return spanName;
    }
}