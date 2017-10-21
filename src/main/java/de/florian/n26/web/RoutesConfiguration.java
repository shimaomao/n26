package de.florian.n26.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
class RoutesConfiguration {

    private RequestHandler requestHandler;

    RoutesConfiguration(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    @Bean
    RouterFunction<?> routes() {
        return
            route(
                GET("/statistics"), requestHandler::loadStatistics)
            .andRoute(
                POST("/transactions").and(accept(APPLICATION_JSON)), requestHandler::addTransaction);
    }

}
