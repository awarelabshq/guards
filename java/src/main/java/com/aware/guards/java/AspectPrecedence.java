package com.aware.guards.java;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclarePrecedence;

@Aspect
@DeclarePrecedence("com.aware.guards.java.Guarded,io.opentelemetry.instrumentation.annotations.WithSpan")
public class AspectPrecedence {

}
