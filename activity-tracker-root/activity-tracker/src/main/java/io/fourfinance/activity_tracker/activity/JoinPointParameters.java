package io.fourfinance.activity_tracker.activity;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.util.List;
import java.util.Optional;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

class JoinPointParameters {

    private final List<String> parameterNames;

    private final List<Object> parameterValues;

    JoinPointParameters(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        parameterNames = signature.getParameterNames() != null ? asList(signature.getParameterNames()) : emptyList();
        parameterValues = joinPoint.getArgs() != null ? asList(joinPoint.getArgs()) : emptyList();
    }

    Optional<Object> getValue(String parameterName) {
        return Optional.ofNullable(parameterNames)
                .filter(p -> p.contains(parameterName))
                .map(p -> parameterValues.get(indexOfParameter(parameterName)));
    }

    private int indexOfParameter(final String parameterName) {
        return parameterNames.indexOf(parameterName);
    }

}
