/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.spring.adaptive;

import com.aizuda.bpm.engine.FlowLongExpression;
import com.aizuda.bpm.engine.model.NodeExpression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/** Spring SpEL expression adapter. */
public class SpelFlowLongExpression implements FlowLongExpression {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_TIME_MILLIS_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private final ExpressionParser parser;

    public SpelFlowLongExpression() {
        parser = new SpelExpressionParser();
    }

    @Override
    public boolean eval(List<List<NodeExpression>> conditionList, Map<String, Object> args) {
        return this.eval(conditionList, () -> args, expr -> {
            StandardEvaluationContext context = new StandardEvaluationContext();
            context.setVariables(args);
            return parser.parseExpression(expr).getValue(context, Boolean.class);
        });
    }

    @Override
    public String exprOfArgs(NodeExpression nodeExpression, Map<String, Object> args) {
        String value = nodeExpression.getValue();
        String operator = nodeExpression.getOperator();
        String field = nodeExpression.getField();
        Object fieldValue = args.get(field);

        if (isTemporal(fieldValue)) {
            String left = temporalExpression(field);
            String right = parseTemporal(value) + "L";
            if (this.include(operator)) {
                return left + " == " + right;
            }
            if (this.notInclude(operator)) {
                return left + " != " + right;
            }
            return left + " " + operator + " " + right;
        }

        if (this.include(operator) || this.notInclude(operator)) {
            String right = literal(value, collectionElement(fieldValue));
            String expression;
            if (fieldValue instanceof Collection) {
                expression = "#" + field + ".contains(" + right + ")";
            } else if (fieldValue != null && fieldValue.getClass().isArray()) {
                expression = "T(java.util.Arrays).asList(#" + field + ").contains(" + right + ")";
            } else {
                // Candidate matching on a scalar is equality, not substring matching.
                expression = "#" + field + (this.include(operator) ? " == " : " != ") + right;
            }
            return this.notInclude(operator) && (fieldValue instanceof Collection
                    || (fieldValue != null && fieldValue.getClass().isArray()))
                    ? "not " + expression : expression;
        }

        return "#" + field + " " + operator + " " + literal(value, fieldValue);
    }

    private String temporalExpression(String field) {
        return "T(" + SpelFlowLongExpression.class.getName() + ").toEpochMillis(#" + field + ")";
    }

    private Object collectionElement(Object value) {
        if (value instanceof Collection) {
            for (Object element : (Collection<?>) value) {
                if (element != null) {
                    return element;
                }
            }
            return null;
        }
        if (value != null && value.getClass().isArray()) {
            for (int i = 0; i < Array.getLength(value); i++) {
                Object element = Array.get(value, i);
                if (element != null) {
                    return element;
                }
            }
        }
        return value;
    }

    private String literal(String value, Object fieldValue) {
        if (value == null) {
            return "null";
        }
        if (isTemporal(fieldValue)) {
            return parseTemporal(value) + "L";
        }
        if (fieldValue instanceof String || fieldValue instanceof Character || fieldValue == null) {
            return "'" + escape(value) + "'";
        }
        if (fieldValue instanceof Boolean) {
            return Boolean.toString(Boolean.parseBoolean(value));
        }
        if (fieldValue instanceof Long) {
            return value.endsWith("L") ? value : value + "L";
        }
        if (fieldValue instanceof BigDecimal) {
            return "new java.math.BigDecimal('" + escape(value) + "')";
        }
        if (fieldValue instanceof BigInteger) {
            return "new java.math.BigInteger('" + escape(value) + "')";
        }
        if (fieldValue instanceof Number) {
            return value;
        }
        return "'" + escape(value) + "'";
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("'", "''");
    }

    private boolean isTemporal(Object value) {
        return value instanceof java.util.Date
                || value instanceof Calendar
                || value instanceof Instant
                || value instanceof LocalDateTime
                || value instanceof LocalDate
                || value instanceof OffsetDateTime
                || value instanceof ZonedDateTime;
    }

    private long parseTemporal(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Temporal condition value must not be null");
        }
        String text = value.trim();
        try {
            return Instant.parse(text).toEpochMilli();
        } catch (DateTimeParseException ignored) {
            // Continue with local and offset date/time formats.
        }
        try {
            return OffsetDateTime.parse(text, DateTimeFormatter.ISO_DATE_TIME).toInstant().toEpochMilli();
        } catch (DateTimeParseException ignored) {
            // Continue with local date/time formats.
        }
        try {
            return ZonedDateTime.parse(text, DateTimeFormatter.ISO_DATE_TIME).toInstant().toEpochMilli();
        } catch (DateTimeParseException ignored) {
            // Continue with local date/time formats.
        }
        for (DateTimeFormatter formatter : new DateTimeFormatter[]{DATE_TIME_FORMATTER, DATE_TIME_MILLIS_FORMATTER,
                DateTimeFormatter.ISO_LOCAL_DATE_TIME}) {
            try {
                return LocalDateTime.parse(text, formatter).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
            } catch (DateTimeParseException ignored) {
                // Try the next supported format.
            }
        }
        try {
            return LocalDate.parse(text, DateTimeFormatter.ISO_LOCAL_DATE)
                    .atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Unsupported temporal condition value: " + value, exception);
        }
    }

    /** Converts supported temporal argument values to generated SpEL's representation. */
    public static Long toEpochMillis(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof java.util.Date) {
            return ((java.util.Date) value).getTime();
        }
        if (value instanceof Calendar) {
            return ((Calendar) value).getTimeInMillis();
        }
        if (value instanceof Instant) {
            return ((Instant) value).toEpochMilli();
        }
        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
        if (value instanceof LocalDate) {
            return ((LocalDate) value).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
        if (value instanceof OffsetDateTime) {
            return ((OffsetDateTime) value).toInstant().toEpochMilli();
        }
        if (value instanceof ZonedDateTime) {
            return ((ZonedDateTime) value).toInstant().toEpochMilli();
        }
        throw new IllegalArgumentException("Unsupported temporal value: " + value.getClass().getName());
    }
}
