package com.fszuberski.wc.application.domain;

import java.util.Map;

public class CountResult {
    private final Map<CountType, Long> resultsPerType;

    private CountResult(Map<CountType, Long> resultsPerType) {
        this.resultsPerType = resultsPerType;
    }

    public Map<CountType, Long> resultsPerType() {
        return resultsPerType;
    }

    public static CountResult of(Map<CountType, Long> resultsPerType) {
        return new CountResult(resultsPerType);
    }

    @Override
    public String toString() {
        return "CountResult{" +
                "resultsPerType=" + resultsPerType +
                '}';
    }
}
