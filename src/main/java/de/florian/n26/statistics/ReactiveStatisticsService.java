package de.florian.n26.statistics;

import de.florian.n26.web.TransactionResource;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ReactiveStatisticsService {

    private final StatisticsService statisticsService;

    public ReactiveStatisticsService(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    public Mono<Boolean> addTransaction(Publisher<TransactionResource> transactionPublisher) {
        return Mono.from(transactionPublisher)
                .flatMap(transaction -> Mono.just(statisticsService.saveTransaction(transaction)))
                .switchIfEmpty(Mono.just(Boolean.FALSE));
    }

    public Mono<Statistics> lastSixtySeconds() {
        return Mono.just(statisticsService.fetchLastSixtySeconds());
    }
}
