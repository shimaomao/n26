package de.florian.n26.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.reactive.function.server.RouterFunction;
import reactor.ipc.netty.http.server.HttpServer;

import static org.springframework.web.reactive.function.server.RouterFunctions.toHttpHandler;

@Configuration
class HttpServerConfiguration {

    @Autowired Environment environment;

    @Bean
    HttpServer httpServer(RouterFunction<?> routerFunction) {
        HttpHandler httpHandler = toHttpHandler(routerFunction);
        ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(httpHandler);
        HttpServer server = HttpServer.create(environment.getProperty("server.address"), Integer.valueOf(environment.getProperty("server.port")));
        server.newHandler(adapter);
        return server;
    }
}