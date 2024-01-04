# Guards User Guide - Java (Spring)

Pre-requisites:

1. Your system needs to be instrumented with [OpenTelemetry](https://opentelemetry.io). Refer [here](https://opentelemetry.io/docs/instrumentation/java/) for guidance on getting started with Java stack.
2. OTel collector is configured to export telemetry data to [Aware Labs](https://awarelabs.io). Refer [here](https://awarelabs.io/blog/getting-started-java) for guidance.

## Enabling `@Guarded` Annotation

* Install guards library via jitpack:

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
3. Make sure the `OpenTelemetry` bean is made available in your Spring application:
```
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
...

@Bean
public OpenTelemetry openTelemetry() {
    return GlobalOpenTelemetry.get();
}
```
4. Add `@Guarded` annotation to functions you want to guard. Refer syntax section below for advanced annotation configuration.

5. Update CI / CD pipeline to call Aware Data API to get evaluated guard results against a given release candidate of a given binary. (Refer Aware Data API reference section below for more details).
   
# `@Guarded` Syntax

`@Guarded` annotation takes the following arguments:

* `since`: (Optional) a date formatted as YYYY-MM-DD. If provided, the guard will be evaluated for releases after the given date.
* `severity`: Defaults to "WARN". Severity of the guard. values: INFO, WARN, FATAL. INFO and WARN level guards will only show up in Aware dashboards while FATAL is for halting the release push.
* `environment`: (Optional) The environment in which to check for the guard condition. For instance, if you have a `staging` environment where testing happens, and you want to ensure that the function is executed in `staging` before a release is pushed, you can set this argument to be `staging`. You can specify a default test environment in Aware project settings, and if no value is provided, the condition will be evaluated in the default test environment.
* `value`: (Optional) Name of the (Otel) span to create for the function (same as `value` argument in `@WithSpan` Otel annotation). If not provided, defaults to create a span using the same naming convention as `@WithSpan` annotation.
* `kind`: (Defaults to SpanKind.INTERNAL) Type of (Otel) span to create (sam as `kind` argument in `@WithSpan` Otel annotation)
* `createNewSpan`: (Defaults to TRUE) Boolean indicating whether to create a new span or add guard metadata to existing current span. If your function is already instrumented as a span, then setting this to false will make sure that the existing span is used for guarding.
* `condition`: (Optional) Condition to verify. Refer condition syntax section below for more details.
* `filters`: (Optional) Additional filters to apply for selecting the traces against which the `condition` is verified. For instance, if the function instruments an attribute "input_size", and you want to verify the `condition` is passing for "large" `input_size`, you can specify this argument to be `"input_size = large"`. Refer filter syntax section below for more details.

## `Condition` Syntax

`condition` argument specifies which conditions to verify for new releases. You can specify a comma separated list of conditions, and a Guard will be created per each condition.
Each condition needs to be of the format: <attribute> <operator> <compare_value>.
The attribute can be any Otel instrumented attribute at the current span. Additionally, there are some special attributes as listed below:
* `avg_latency`: Average latency of the operation
* `max_latency`: Max latency of the operation
* `min_latency`: Min latency of the operation

Following Operators are currently supported:
* `=`: Checks for equality
* `<`: Less than
* `>`: Greater than
* `!=`: Not equals
* `contains`: Contains the given `compare_value`. For instance, say you want to make sure the function is tested for attribute `country_code` : `US`. This can be achieved with filter condition: `"country_code contains US"`. If the presence of multiple values need to be verified, they can be provided as a list of items separated with `|` operator. So if you want to ensure that your `doPay()` function is tested with all payment methods, you can add a condition: `"payment_method contains VISA|MASTERCARD|AMEX"`.
* `not contains`: Not contains the given `compare_value`.

## `Filter` Syntax

`filter` argument enables narrowing down the scope of the traces which are used for evaluating the `condition`.  
For instance, if you have a `input_size` attribute and you want to make sure the `condition`: `"avg_latency < 1000"` is verified for "large" `input_size`s, you can achieve it by specifying `filters="input_size = large"`.  
Similar to `condition`, `filters` also takes a comma separated list of expressions (of the format "<attribute> <operator> <compare_value>") and all filters are applied to select the traces.

Following Operators are currently supported:
* `=`: Checks for equality
* `!=`: Not equals
* `contains`: Contains
* `not contains`: Not contains

## Aware Data API Integration Guide

When you create a project in [Aware Labs](https://awarelabs.io), you get a unique API-Key for the project. You can call Aware Data API endpoint for getting evaluated guards for a given release candidate as follows:

Endpoint: 
```
https://featureservice.awarelabs.io/data/evaluate_guards
```
Call with http header `Aware-Api-Key` set to the unique API-Key for your project, and `project-id` http header set to your unique project id (Can be viewed in Aware Platform -> Project Settings -> General).

Request Body:
```
{
    "filter": {
        "resource": "<NAME OF RESOURCE (eg: frontend)>"
    },
    "evaluation_context":{
        "resource_version":"<RELEASE CANDIDATE VERSION>",
        "environment":"(Optional) The environment against which the guards should be evaluated. If not present, evaluation will rely on environments specified in individual guards, and fallback to use the default test environment configured in project settings in Aware Platform".
    }
}
```

The above request can be made from your CI / CD pipeline, and the response will be of the following format:
```
{
  "guards": [{
    "guard": {
      "id": "",
      "config": {
        ...
        "severity": "INFO|WARN|FATAL"
      }
    },
    "evaluation": {
      "result": "<Evaluation Result> (FAIL | SUCCESS)",
      "leftValue": "(Evaluated value)",
      "rightValue": "(Expected value)"
    }
  }]
}
```
You can then use the response to halt the CI / CD pipeline if `FATAL` severity guards are failing if needed.
