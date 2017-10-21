package de.florian.n26.web;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class StatisticsResource {

    private final long count;
    private final double sum;
    private final double min;
    private final double max;
    private final double average;

    @JsonCreator
    public StatisticsResource(@JsonProperty("count") long count,
                              @JsonProperty("sum") double sum,
                              @JsonProperty("min") double min,
                              @JsonProperty("max") double max,
                              @JsonProperty("average") double average) {
        this.count = count;
        this.sum = sum;
        this.min = min;
        this.max = max;
        this.average = average;
    }

    public long getCount() {
        return count;
    }

    public double getSum() {
        return sum;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getAverage() {
        return average;
    }

}
