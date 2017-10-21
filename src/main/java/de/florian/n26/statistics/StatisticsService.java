package de.florian.n26.statistics;

import de.florian.n26.web.TransactionResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class StatisticsService {

    private static final long RETENTION_MILLIS = TimeUnit.SECONDS.toMillis(60);

    private final ConcurrentMap<Long, Statistics> statistics;
    private final Clock clock;

    public StatisticsService() {
        this.statistics = new ConcurrentHashMap<>();
        this.clock = Clock.systemUTC();
    }

    protected StatisticsService(Clock clock) {
        this.statistics = new ConcurrentHashMap<>();
        this.clock = clock;

    }

    public Statistics fetchLastSixtySeconds() {
        final long now = clock.millis();
        return statistics.entrySet().stream()
                .filter(entry -> notTooOld(entry.getKey(), now) && entry.getKey() < now)
                .flatMap(entry -> Stream.of(entry.getValue()))
                .reduce(Statistics::combine).orElse(new Statistics(0, 0, 0, 0));
    }

    public boolean saveTransaction(TransactionResource transaction) {
        if (notTooOld(transaction.getTimestamp(), clock.millis())) {
            statistics.merge(
                    transaction.getTimestamp(),
                    Statistics.fromSingleTransaction(transaction),
                    Statistics::combine);
            return true;
        }
        return false;
    }

    private boolean notTooOld(long timeStamp, long now) {
        return timeStamp > (now - RETENTION_MILLIS);
    }

    @Scheduled(fixedDelay = 120000)
    public void cleanUp() {
        long now = clock.millis();
        Set<Long> old = statistics.keySet().stream().filter(key -> !notTooOld(key, now)).collect(Collectors.toSet());
        old.forEach(statistics::remove);
    }
}
