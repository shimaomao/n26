package de.florian.n26.statistics;

import de.florian.n26.web.StatisticsResource;
import de.florian.n26.web.TransactionResource;

public class Statistics {

    private final long count;
    private final double sum;
    private final double min;
    private final double max;

    public Statistics(long count, double sum, double min, double max) {
        this.count = count;
        this.sum = sum;
        this.min = min;
        this.max = max;
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
        if (count == 0) {
            return 0d;
        }
        return sum / count;
    }

    public StatisticsResource toResource() {
        return new StatisticsResource(count, sum, min, max, getAverage());
    }

    public Statistics combine(Statistics other) {
        return new Statistics(
                count + other.count,
                sum + other.sum,
                Double.min(min, other.min),
                Double.max(max, other.max)
        );
    }

    public static Statistics fromSingleTransaction(TransactionResource transaction) {
        return new Statistics(1l, transaction.getAmount(), transaction.getAmount(), transaction.getAmount());
    }
}
