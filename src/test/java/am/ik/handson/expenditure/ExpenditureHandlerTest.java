package am.ik.handson.expenditure;

import am.ik.handson.App;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.net.URI;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExpenditureHandlerTest {

    private WebTestClient testClient;

    private InMemoryExpenditureRepository expenditureRepository = new InMemoryExpenditureRepository();

    private ExpenditureHandler expenditureHandler = new ExpenditureHandler(this.expenditureRepository);

    private List<Expenditure> fixtures = Arrays.asList(
        new ExpenditureBuilder()
            .withExpenditureId(1)
            .withExpenditureName("本")
            .withPrice(2000)
            .withQuantity(1)
            .withExpenditureDate(LocalDate.of(2019, 4, 1))
            .createExpenditure(),
        new ExpenditureBuilder()
            .withExpenditureId(2)
            .withExpenditureName("コーヒー")
            .withPrice(300)
            .withQuantity(2)
            .withExpenditureDate(LocalDate.of(2019, 4, 2))
            .createExpenditure());

    @BeforeAll
    void before() {
        this.testClient = WebTestClient.bindToRouterFunction(this.expenditureHandler.routes())
            .handlerStrategies(App.handlerStrategies())
            .build();
    }

    @BeforeEach
    void reset() {
        this.expenditureRepository.expenditures.clear();
        this.expenditureRepository.expenditures.addAll(this.fixtures);
        this.expenditureRepository.counter.set(100);
    }

    @Test
    void list() {
        this.testClient.get()
            .uri("/expenditures")
            .exchange()
            .expectStatus().isOk()
            .expectBody(JsonNode.class)
            .consumeWith(result -> {
                JsonNode body = result.getResponseBody();
                assertThat(body).isNotNull();
                assertThat(body.size()).isEqualTo(2);

                assertThat(body.get(0).get("expenditureId").asInt()).isEqualTo(1);
                assertThat(body.get(0).get("expenditureName").asText()).isEqualTo("本");
                assertThat(body.get(0).get("price").asInt()).isEqualTo(2000);
                assertThat(body.get(0).get("quantity").asInt()).isEqualTo(1);
                assertThat(body.get(0).get("expenditureDate").asText()).isEqualTo("2019-04-01");

                assertThat(body.get(1).get("expenditureId").asInt()).isEqualTo(2);
                assertThat(body.get(1).get("expenditureName").asText()).isEqualTo("コーヒー");
                assertThat(body.get(1).get("price").asInt()).isEqualTo(300);
                assertThat(body.get(1).get("quantity").asInt()).isEqualTo(2);
                assertThat(body.get(1).get("expenditureDate").asText()).isEqualTo("2019-04-02");
            });
    }

    @Test
    void get_200() {
        this.testClient.get()
            .uri("/expenditures/1")
            .exchange()
            .expectStatus().isOk()
            .expectBody(JsonNode.class)
            .consumeWith(result -> {
                JsonNode body = result.getResponseBody();
                assertThat(body).isNotNull();

                assertThat(body.get("expenditureId").asInt()).isEqualTo(1);
                assertThat(body.get("expenditureName").asText()).isEqualTo("本");
                assertThat(body.get("price").asInt()).isEqualTo(2000);
                assertThat(body.get("quantity").asInt()).isEqualTo(1);
                assertThat(body.get("expenditureDate").asText()).isEqualTo("2019-04-01");
            });
    }

    @Test
    void get_404() {
        this.testClient.get()
            .uri("/expenditures/10000")
            .exchange()
            .expectStatus().isNotFound()
            .expectBody(JsonNode.class)
            .consumeWith(result -> {
                JsonNode body = result.getResponseBody();
                assertThat(body).isNotNull();

                assertThat(body.get("status").asInt()).isEqualTo(404);
                assertThat(body.get("error").asText()).isEqualTo("Not Found");
                assertThat(body.get("message").asText()).isEqualTo("The given expenditure is not found.");
            });
    }

    @Test
    void post() {
        Expenditure expenditure = new ExpenditureBuilder()
            .withExpenditureName("ビール")
            .withPrice(250)
            .withQuantity(1)
            .withExpenditureDate(LocalDate.of(2019, 4, 3))
            .createExpenditure();

        this.testClient.post()
            .uri("/expenditures")
            .syncBody(expenditure)
            .exchange()
            .expectStatus().isCreated()
            .expectBody(JsonNode.class)
            .consumeWith(result -> {
                URI location = result.getResponseHeaders().getLocation();
                assertThat(location.toString()).isEqualTo("/expenditures/100");
                JsonNode body = result.getResponseBody();
                assertThat(body).isNotNull();

                assertThat(body.get("expenditureId").asInt()).isEqualTo(100);
                assertThat(body.get("expenditureName").asText()).isEqualTo("ビール");
                assertThat(body.get("price").asInt()).isEqualTo(250);
                assertThat(body.get("quantity").asInt()).isEqualTo(1);
                assertThat(body.get("expenditureDate").asText()).isEqualTo("2019-04-03");
            });

        this.testClient.get()
            .uri("/expenditures/100")
            .exchange()
            .expectStatus().isOk()
            .expectBody(JsonNode.class)
            .consumeWith(result -> {
                JsonNode body = result.getResponseBody();
                assertThat(body).isNotNull();

                assertThat(body.get("expenditureId").asInt()).isEqualTo(100);
                assertThat(body.get("expenditureName").asText()).isEqualTo("ビール");
                assertThat(body.get("price").asInt()).isEqualTo(250);
                assertThat(body.get("quantity").asInt()).isEqualTo(1);
                assertThat(body.get("expenditureDate").asText()).isEqualTo("2019-04-03");
            });
    }

    @Test
    void delete() {
        this.testClient.delete()
            .uri("/expenditures/1")
            .exchange()
            .expectStatus().isNoContent();

        this.testClient.get()
            .uri("/expenditures/1")
            .exchange()
            .expectStatus().isNotFound();
    }
}