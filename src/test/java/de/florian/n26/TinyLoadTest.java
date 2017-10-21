package de.florian.n26;

import de.florian.n26.web.StatisticsResource;
import de.florian.n26.web.TransactionResource;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.time.OffsetDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class TinyLoadTest {

    private WebTestClient client;

    @Before
    public void setup() {
        client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + "8080")
                .build();
    }

    @Test
    @Ignore // run manually
    public void loadTest() throws Exception {

        AtomicInteger atomicInteger = new AtomicInteger();

        ExecutorService executorService = Executors.newFixedThreadPool(15);
        int numberOfThreads = 10;

        Runnable producer = () -> {
            WebTestClient client = WebTestClient.bindToServer()
                    .baseUrl("http://localhost:" + "8080")
                    .build();

            IntStream
                    .rangeClosed(0, 10000000)
                    .forEach(index -> {
                        client.post().uri("/transactions")
                                .body(BodyInserters.fromObject(new TransactionResource(OffsetDateTime.now().toInstant().toEpochMilli(), 12.5)))
                                .exchange()
                                .expectStatus().isEqualTo(HttpStatus.CREATED);
                        atomicInteger.getAndIncrement();
                    });

        };

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.execute(producer);
        }

        Thread.sleep(20000);
        EntityExchangeResult<StatisticsResource> result = client.get().uri("/statistics").accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectStatus().isOk()
                .expectBody(StatisticsResource.class).returnResult();

        long count = atomicInteger.get();
        StatisticsResource statisticsResource = result.getResponseBody();

        assertThat(statisticsResource.getCount()).isBetween(count - 100, count);

//        Thread.sleep(100000);
    }
}