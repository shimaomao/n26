package de.florian.n26;

import de.florian.n26.web.StatisticsResource;
import de.florian.n26.web.TransactionResource;
import org.junit.Before;
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

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class IntegrationTests {

    private WebTestClient client;

    @Before
    public void setup() {
        client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + "8080")
                .build();
    }
    @Test
    public void testStatistics() throws Exception {

        EntityExchangeResult<StatisticsResource> result = client.get().uri("/statistics").accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectStatus().isOk()
                .expectBody(StatisticsResource.class).returnResult();

        StatisticsResource statisticsResource = result.getResponseBody();

        assertThat(statisticsResource.getCount()).isBetween(0l, 1l); // can't be sure
    }

    @Test
    public void testTransactions() throws Exception {

        client.post().uri("/transactions")
                .body(BodyInserters.fromObject(new TransactionResource(OffsetDateTime.now().toInstant().toEpochMilli(), 12.5)))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CREATED);
    }

}