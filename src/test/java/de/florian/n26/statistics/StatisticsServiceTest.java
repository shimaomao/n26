package de.florian.n26.statistics;

import de.florian.n26.statistics.Statistics;
import de.florian.n26.statistics.StatisticsService;
import de.florian.n26.web.TransactionResource;
import org.assertj.core.data.Offset;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StatisticsServiceTest {

    @Test
    public void whenTransactionTooOld_addTransactionShouldReturnFalse() throws Exception {

        LocalDateTime now = LocalDateTime.of(2017, 10, 11, 12, 13, 14, 15);
        Clock clock = Clock.fixed(now.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);

        StatisticsService statisticsService = new StatisticsService(clock);

        long oldTimeStamp = now.minusSeconds(61).toInstant(ZoneOffset.UTC).toEpochMilli();

        TransactionResource tooOld = new TransactionResource(oldTimeStamp, 123d);

        boolean inserted = statisticsService.saveTransaction(tooOld);

        assertThat(inserted).isFalse();
    }

    @Test
    public void whenTransactionInTheFuture_shouldBeSaved() throws Exception {

        LocalDateTime now = LocalDateTime.of(2017, 10, 11, 12, 13, 14, 15);
        Clock clock = Clock.fixed(now.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);

        StatisticsService statisticsService = new StatisticsService(clock);

        long futureTimeStamp = now.plusSeconds(61).toInstant(ZoneOffset.UTC).toEpochMilli();

        TransactionResource futureTransaction = new TransactionResource(futureTimeStamp, 123d);

        boolean inserted = statisticsService.saveTransaction(futureTransaction);

        assertThat(inserted).isTrue();
    }

    @Test
    public void whenLoadingStatistics_shouldIgnoreOldTransactions() throws Exception {

        LocalDateTime now = LocalDateTime.of(2017, 10, 11, 12, 13, 14, 15);
        LocalDateTime earlier = now.minusSeconds(15);
        LocalDateTime evenEarlier = earlier.minusSeconds(15);

        LocalDateTime later = evenEarlier.plusSeconds(65);

        long currentTimeStamp = now.toInstant(ZoneOffset.UTC).toEpochMilli();
        long earlierTimestamp = earlier.toInstant(ZoneOffset.UTC).toEpochMilli();
        long evenEarlierTimestamp = evenEarlier.toInstant(ZoneOffset.UTC).toEpochMilli();
        long laterTimeStamp = later.toInstant(ZoneOffset.UTC).toEpochMilli();

        Clock mockedClock = mock(Clock.class);
        when(mockedClock.millis()).thenReturn(currentTimeStamp, currentTimeStamp, currentTimeStamp, laterTimeStamp);

        StatisticsService statisticsService = new StatisticsService(mockedClock);

        // insert two transactions at slightly different points in the past
        TransactionResource earlierTransaction = new TransactionResource(earlierTimestamp, 10);
        TransactionResource evenEarlierTransaction = new TransactionResource(evenEarlierTimestamp, 20d);
        assertThat(statisticsService.saveTransaction(earlierTransaction)).isTrue();
        assertThat(statisticsService.saveTransaction(evenEarlierTransaction)).isTrue();

        Mockito.verify(mockedClock, Mockito.times(2)).millis();

        // currently, they should _both_ be in the statistics
        Statistics statistics = statisticsService.fetchLastSixtySeconds();

        Mockito.verify(mockedClock, Mockito.times(3)).millis();

        assertThat(statistics.getCount()).isEqualTo(2);
        assertThat(statistics.getSum()).isCloseTo(30d, Offset.offset(0.001));
        assertThat(statistics.getAverage()).isCloseTo(15d, Offset.offset(0.001));
        assertThat(statistics.getMin()).isCloseTo(10d, Offset.offset(0.001));
        assertThat(statistics.getMax()).isCloseTo(20d, Offset.offset(0.001));

        // now we are in the future (see thenReturn...) so we should not see the oldest transaction any more
        Statistics otherStatistics = statisticsService.fetchLastSixtySeconds();

        Mockito.verify(mockedClock, Mockito.times(4)).millis();

        assertThat(otherStatistics.getCount()).isEqualTo(1);
        assertThat(otherStatistics.getSum()).isCloseTo(10d, Offset.offset(0.001));
        assertThat(otherStatistics.getAverage()).isCloseTo(10d, Offset.offset(0.001));
        assertThat(otherStatistics.getMin()).isCloseTo(10d, Offset.offset(0.001));
        assertThat(otherStatistics.getMax()).isCloseTo(10d, Offset.offset(0.001));
    }

    @Test
    public void whenSavingTransactions_shouldSaveFutureTransactions() throws Exception {
        LocalDateTime now = LocalDateTime.of(2017, 10, 11, 12, 13, 14, 15);
        LocalDateTime future = now.plusSeconds(15);

        long currentTimeStamp = now.toInstant(ZoneOffset.UTC).toEpochMilli();
        long futureTimestamp = future.toInstant(ZoneOffset.UTC).toEpochMilli();

        Clock mockedClock = mock(Clock.class);
        when(mockedClock.millis()).thenReturn(currentTimeStamp);

        StatisticsService statisticsService = new StatisticsService(mockedClock);

        TransactionResource futureTransaction = new TransactionResource(futureTimestamp, 10);
        assertThat(statisticsService.saveTransaction(futureTransaction)).isTrue();
    }

    @Test
    public void whenLoadingStatistics_shouldIgnoreFutureTransactions() throws Exception {
        LocalDateTime now = LocalDateTime.of(2017, 10, 11, 12, 13, 14, 15);
        LocalDateTime future = now.plusSeconds(15);

        long currentTimeStamp = now.toInstant(ZoneOffset.UTC).toEpochMilli();
        long futureTimestamp = future.toInstant(ZoneOffset.UTC).toEpochMilli();

        Clock mockedClock = mock(Clock.class);
        when(mockedClock.millis()).thenReturn(currentTimeStamp);

        StatisticsService statisticsService = new StatisticsService(mockedClock);

        TransactionResource futureTransaction = new TransactionResource(futureTimestamp, 10);
        assertThat(statisticsService.saveTransaction(futureTransaction)).isTrue();

        Statistics statistics = statisticsService.fetchLastSixtySeconds();

        assertThat(statistics.getCount()).isEqualTo(0);
    }
}
