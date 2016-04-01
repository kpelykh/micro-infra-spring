package io.fourfinance.activity_tracker.activity;

import static java.util.Optional.ofNullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

public class DefaultTrackUserActivityMetrics implements TrackUserActivityMetrics {

    private final MetricRegistry metricRegistry;
    private final Map<String, Meter> metrics = new HashMap<>();

    public DefaultTrackUserActivityMetrics(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    @Override
    public void process(String activityName) {
        if (Objects.nonNull(activityName)) {
            Optional<Meter> metric = getMetric(activityName);
            if (!metric.isPresent()) {
                metric = initializeMetric(activityName);
            }
            metric.ifPresent(Meter::mark);
        }
    }

    private Optional<Meter> initializeMetric(String activityName) {
        Meter meter = metricRegistry.meter("ACTIVITY." + activityName);
        metrics.put(activityName, meter);
        return Optional.of(meter);
    }

    private Optional<Meter> getMetric(String activityName) {
        return ofNullable(metrics.get(activityName));
    }
}
