package com.go2super.database.entity.sub;

import lombok.*;

import java.util.*;

@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString
@Setter
@Getter
public class UserMetrics {

    private List<Metric> metrics;

    public void add(String metricIdentifier, int value) {

        getMetric(metricIdentifier).add(value);
    }

    public void sub(String metricIdentifier, int value) {

        getMetric(metricIdentifier).sub(value);
    }

    public void reset(String metricIdentifier) {

        getMetric(metricIdentifier).reset();
    }

    public Metric getMetric(String metricIdentifier) {

        if (metrics == null) {
            metrics = new ArrayList<>();
        }
        Optional<Metric> optionalMetric = metrics.stream().filter(m -> m.getIdentifier().equals(metricIdentifier)).findFirst();
        if (optionalMetric.isPresent()) {
            return optionalMetric.get();
        }
        Metric metric = Metric.builder()
            .identifier(metricIdentifier)
            .value(0)
            .build();
        metrics.add(metric);
        return metric;
    }

}
