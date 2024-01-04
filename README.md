# Aware Guards
[![](https://jitpack.io/v/awarelabshq/guards.svg)](https://jitpack.io/#awarelabshq/guards)

  
> Empowering developers to _“guard their functions”_ from being pushed to production without being properly tested or meeting performance expectations.
  

When a new release candidate is being pushed to Production, there are many expectations about the system that needs to be ensured. The typical gating mechanism is running all automation tests as part of the CI / CD pipeline and ensuring the tests are passing. But this leaves room for a lot of issues / inefficiencies to leak into production.

For starters, what if there is no automation test covering a certain conditional path in a flow? Though all your automation tests may be passing, if the tests aren’t covering all execution paths / scenarios in your system, then tests passing isn’t a sufficient condition to ensure the release is stable enough to be pushed.

If you are incorporating manual QA as part of your release process, then this becomes even more trickier. Say a backend developer added a new path for a certain flow. How do you ensure that the manual QA testers have tested that path in the new release candidate? Can we empower developers to highlight it while writing up the code, so that if it is missed, they will know?

Enter - [Aware Guards](https://awarelabs.io/blog/guards).

With Aware Guards, you can create guard conditions detailing various expectations at any operation (regarding any arbitrary attribute - such as `latency`, `error rate`, or a domain specific manually instrumented attribute) - enabling significantly more expressiveness in defining _“expectations that need to be met for pushing a release”_. Here are some examples:
 

1. Ensure the newly added `newConditionalPath()` function is tested by manual QA testing
2. Ensure that the latency of `operationFoo` is at max 500ms during stress testing of the release
3. Ensure that the `PayFlow API` is tested with all payment methods supported by the product in automation testing

Since Aware Guards rely on OpenTelemetry Traces / Spans, they are:
* Tech stack agnostic (works anywhere OTel works) 
* Test approach agnostic (works with Manual QA, Load Testing, Stress Testing, Automation Testing etc.) 

## Guard Functions with `@Guarded`

With Aware Guard Annotations (the scope of this Open Source Project), developers can specify expectations on testing / performance directly via code - making it frictionless to specify guards at individual function level in an intuitive manner, and empowers developers to _“guard their functions”_ from being pushed to production without being properly tested or meeting performance expectations.

### Guard Recipes

Say you are a developer writing / updating a function `doComplexThing()`. 

* Make sure the operation is executed in the default test environment? Annotate it with `@Guarded` like below:

```
@Guarded
doComplexThing()
```

* Ensure that the function is executed in all releases being pushed after a given date (Since you may not want to block earlier releases already in the pipeline):

```
@Guarded( since = “2024-01-10” )
doComplexThing()
```

* Ensure that the average latency of the operation is < 500ms in load testing:

```
@Guarded( condition = “avg_latency < 500”, environment = “load”)
doComplexThing()
```

* Ensure that the function is tested for large inputs in automation testing:

```
@Guarded( filters = “input_size > 1000”, environment = “cypress-staging” )
doComplexThing(List<Object> input){
	Span.current().setAttribute(“input_size”,input.size());
}
```

* Ensure that the function is tested for large inputs, and it is run with reasonable latency in load test:

```
@Guarded( filters = “input_size > 1000”, condition = “avg_latency < 1000”, environment = “load” )
doComplexThing(List<Object> input){
	Span.current().setAttribute(“input_size”,input.size());
}
```

* Don't halt the release, just inform:

```  
@Guarded ( severity = “INFO” )
doComplexThing()
```

## Guards as a way to keep track of assumptions, and where to optimize your Stack

Guards allow expressing various assumptions developers make while writing code. For instance, you may “assume” that the cache-hit rate of the function is 70%. You may choose to go with a different approach if the assumption is inaccurate. You may expect the latency of the operation to be < 1000ms. But if it isn’t true, you may choose to further optimize the step. But as code in your system grows, it becomes hard to keep track of what assumptions were made and where there are opportunities for optimizations - resulting in performance degradations that become hard to tackle. 


By simply annotating key functions with `@Guarded` annotation (and specifying the assumptions as conditions), you can quickly identify when those assumptions are broken at their earliest (and at finer granularities). Aware Guards UI shows the status of the guards for a given resource, providing a quick map of where improvements can be made in your stack.

## How It Works

Prerequisites: 
1. The system needs to be instrumented with [OpenTelemetry](https://opentelemetry.io/).
2. Exporting telemetry data to [Aware Labs](https://awarelabs.io) (Refer [here](https://awarelabs.io/blog/getting-started) to get started with Aware Labs).

When a function is annotated with `@Guarded` annotation, it causes an aspect to be run (implemented in a language specific manner) which creates a Span in the current Trace for the annotated function. The Aspect then attaches guard metadata extracted from the annotation arguments (such as severity, condition etc.) as attributes of the created Span. These attributes are then consumed by [Aware Platform](https://awarelabs.io) to create Guards which can be evaluated against a specific release version of a given resource binary (eg: Evaluate all guards in `frontend` service for release version `2024.01.10.1`). You can call Aware Data API from your CI / CD pipeline to get guard evaluation results (for halting pushes on FATAL guard failures). Aware Web UI shows the status of all active guards and their evaluations - which serves as a map of "where to improve your stack".

## Usage Guide

0. Ensure the prerequisites are met (Your stack is instrumented with OpenTelemetry and exporting data to Aware Platform).
1. Add the jitpack repository in your gradle files' repository section:

```
		repositories {
			maven { url 'https://jitpack.io' }
		}
```

2. Add guards library dependency:

```
	dependencies {
	        implementation 'com.github.awarelabshq:guards:0.0.359'
	}
```

3. Thats it! You can now annotate your functions with `@Guarded` annotation.
