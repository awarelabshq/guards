package com.aware.guards.java;

import io.opentelemetry.api.trace.Span;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class GuardFactory {

    private static final Logger logger = Logger.getLogger(GuardFactory.class.getName());

    public static GuardBuilder createGuard() {
        return new GuardBuilder();
    }

    public static class GuardBuilder {
        private String condition;
        private String environment;
        private Severity severity;
        private List<String> filters;
        private String guardStartDate;

        GuardBuilder() {
            condition = "";
            environment = "";
            guardStartDate = "";
            severity = Severity.WARN;
            filters = new ArrayList<>();
        }

        /**
         * @param condition Condition to guard against. Refer https://github.com/awarelabshq/guards/blob/main/java/user_guide.md#condition-syntax for more details.
         * @return
         */
        public GuardBuilder forCondition(String condition) {
            this.condition = condition;
            return this;
        }

        /**
         * @param environment Environment to check this guard in.
         * @return
         */
        public GuardBuilder inEnvironment(String environment) {
            this.environment = environment;
            return this;
        }

        /**
         * @param severity Severity of this guard. Defaults to WARN.
         * @return
         */
        public GuardBuilder atSeverity(Severity severity) {
            this.severity = severity;
            return this;
        }

        /**
         * @param filters list of filters to apply. Refer https://github.com/awarelabshq/guards/blob/main/java/user_guide.md#filter-syntax for more details.
         * @return
         */
        public GuardBuilder withFilters(List<String> filters) {
            this.filters = filters;
            return this;
        }

        /**
         * @param date date in YYYY-MM-DD format.
         * @return
         */
        public GuardBuilder since(String date) {
            this.guardStartDate = date;
            return this;
        }

        public void guard() {
            Span currentSpan = Span.current();
            logger.fine("Guard invoked");
            if (currentSpan != null) {
                if (condition != null && !condition.trim().isEmpty()) {
                    currentSpan.setAttribute(Constants.AWARE_GUARD_ATTRIB_CONDITION, condition);
                }
                if (environment != null && !environment.trim().isEmpty()) {
                    currentSpan.setAttribute(Constants.AWARE_GUARD_ATTRIB_ENVIRONMENT, environment);
                }
                if (severity != null) {
                    currentSpan.setAttribute(Constants.AWARE_GUARD_ATTRIB_SEVERITY, severity.toString());
                }
                if (!filters.isEmpty()) {
                    currentSpan.setAttribute(Constants.AWARE_GUARD_ATTRIB_FILTERS, String.join(",", filters));
                }
                if (guardStartDate != null && !guardStartDate.trim().isEmpty()) {
                    currentSpan.setAttribute(Constants.AWARE_GUARD_ATTRIB_SINCE, guardStartDate);
                }
            }
        }
    }
}