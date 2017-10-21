package de.florian.n26.web;

import de.florian.n26.statistics.ReactiveStatisticsService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;

@Component
class RequestHandler {

    private final ReactiveStatisticsService reactiveStatisticsService;

    RequestHandler(ReactiveStatisticsService reactiveStatisticsService) {
        this.reactiveStatisticsService = reactiveStatisticsService;
    }

    Mono<ServerResponse> loadStatistics(ServerRequest serverRequest) {
        return reactiveStatisticsService.lastSixtySeconds()
                .flatMap(statistics -> ServerResponse.ok().contentType(APPLICATION_JSON).body(fromObject(statistics.toResource())))
                .switchIfEmpty(ServerResponse.badRequest().build());
    }

    Mono<ServerResponse> addTransaction(ServerRequest serverRequest) {
        return serverRequest
                .body(BodyExtractors.toMono(TransactionResource.class))
                .publish(this::publishTransaction)
                .flatMap(created -> created ? ServerResponse.status(HttpStatus.CREATED).build() : ServerResponse.status(HttpStatus.NO_CONTENT).build());
    }

    private Mono<Boolean> publishTransaction(Mono<TransactionResource> transactionPublisher) {
        return reactiveStatisticsService.addTransaction(transactionPublisher);
    }
}
