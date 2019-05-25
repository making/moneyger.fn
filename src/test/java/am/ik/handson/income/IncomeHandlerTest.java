package am.ik.handson.income;

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

public class IncomeHandlerTest {

    private WebTestClient testClient;

    private InMemoryIncomeRepository incomeRepository = new InMemoryIncomeRepository();

    private IncomeHandler incomeHandler = new IncomeHandler(this.incomeRepository);

    private List<Income> fixtures = Arrays.asList(
        new IncomeBuilder()
            .withIncomeId(1)
            .withIncomeName("給与")
            .withAmount(200000)
            .withIncomeDate(LocalDate.of(2019, 4, 15))
            .createIncome(),
        new IncomeBuilder()
            .withIncomeId(2)
            .withIncomeName("ボーナス")
            .withAmount(150000)
            .withIncomeDate(LocalDate.of(2019, 4, 25))
            .createIncome());

    @BeforeAll
    void before() {
        this.testClient = WebTestClient.bindToRouterFunction(this.incomeHandler.routes())
            .handlerStrategies(App.handlerStrategies())
            .build();
    }

    @BeforeEach
    void reset() {
        this.incomeRepository.incomes.clear();
        this.incomeRepository.incomes.addAll(this.fixtures);
        this.incomeRepository.counter.set(100);
    }

    @Test
    void list() {
        this.testClient.get()
            .uri("/incomes")
            .exchange()
            .expectStatus().isOk()
            .expectBody(JsonNode.class)
            .consumeWith(result -> {
                JsonNode body = result.getResponseBody();
                assertThat(body).isNotNull();
                assertThat(body.size()).isEqualTo(2);

                assertThat(body.get(0).get("incomeId").asInt()).isEqualTo(1);
                assertThat(body.get(0).get("incomeName").asText()).isEqualTo("給与");
                assertThat(body.get(0).get("amount").asInt()).isEqualTo(200000);
                assertThat(body.get(0).get("incomeDate").asText()).isEqualTo("2019-04-15");

                assertThat(body.get(1).get("incomeId").asInt()).isEqualTo(2);
                assertThat(body.get(1).get("incomeName").asText()).isEqualTo("ボーナス");
                assertThat(body.get(1).get("amount").asInt()).isEqualTo(150000);
                assertThat(body.get(1).get("incomeDate").asText()).isEqualTo("2019-04-25");
            });
    }

    @Test
    void get_200() {
        this.testClient.get()
            .uri("/incomes/1")
            .exchange()
            .expectStatus().isOk()
            .expectBody(JsonNode.class)
            .consumeWith(result -> {
                JsonNode body = result.getResponseBody();
                assertThat(body).isNotNull();

                assertThat(body.get("incomeId").asInt()).isEqualTo(1);
                assertThat(body.get("incomeName").asText()).isEqualTo("給与");
                assertThat(body.get("amount").asInt()).isEqualTo(200000);
                assertThat(body.get("incomeDate").asText()).isEqualTo("2019-04-15");
            });
    }

    @Test
    void get_404() {
        this.testClient.get()
            .uri("/incomes/10000")
            .exchange()
            .expectStatus().isNotFound()
            .expectBody(JsonNode.class)
            .consumeWith(result -> {
                JsonNode body = result.getResponseBody();
                assertThat(body).isNotNull();

                assertThat(body.get("status").asInt()).isEqualTo(404);
                assertThat(body.get("error").asText()).isEqualTo("Not Found");
                assertThat(body.get("message").asText()).isEqualTo("The given income is not found.");
            });
    }

    @Test
    void post() {
        Income income = new IncomeBuilder()
            .withIncomeName("臨時収入")
            .withAmount(250000)
            .withIncomeDate(LocalDate.of(2019, 4, 28))
            .createIncome();

        this.testClient.post()
            .uri("/incomes")
            .syncBody(income)
            .exchange()
            .expectStatus().isCreated()
            .expectBody(JsonNode.class)
            .consumeWith(result -> {
                URI location = result.getResponseHeaders().getLocation();
                assertThat(location.toString()).isEqualTo("/incomes/100");
                JsonNode body = result.getResponseBody();
                assertThat(body).isNotNull();

                assertThat(body.get("incomeId").asInt()).isEqualTo(100);
                assertThat(body.get("incomeName").asText()).isEqualTo("臨時収入");
                assertThat(body.get("amount").asInt()).isEqualTo(250000);
                assertThat(body.get("incomeDate").asText()).isEqualTo("2019-04-28");
            });

        this.testClient.get()
            .uri("/incomes/100")
            .exchange()
            .expectStatus().isOk()
            .expectBody(JsonNode.class)
            .consumeWith(result -> {
                JsonNode body = result.getResponseBody();
                assertThat(body).isNotNull();

                assertThat(body.get("incomeId").asInt()).isEqualTo(100);
                assertThat(body.get("incomeName").asText()).isEqualTo("臨時収入");
                assertThat(body.get("amount").asInt()).isEqualTo(250000);
                assertThat(body.get("incomeDate").asText()).isEqualTo("2019-04-28");
            });
    }

    @Test
    void delete() {
        this.testClient.delete()
            .uri("/incomes/1")
            .exchange()
            .expectStatus().isNoContent();

        this.testClient.get()
            .uri("/incomes/1")
            .exchange()
            .expectStatus().isNotFound();
    }
}