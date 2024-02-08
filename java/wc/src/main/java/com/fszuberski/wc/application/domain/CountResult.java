package com.fszuberski.wc.application.domain;

import java.util.Map;
import java.util.Set;

public class CountResult {
    private final Map<CountType, Long> resultsPerType;

    private CountResult(Map<CountType, Long> resultsPerType) {
        this.resultsPerType = resultsPerType;
    }

    public Set<Map.Entry<CountType, Long>> entries() {
        return resultsPerType.entrySet();
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
