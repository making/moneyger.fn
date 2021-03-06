package am.ik.handson.income;

import am.ik.handson.App;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.net.URI;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.epages.restdocs.apispec.WebTestClientRestDocumentationWrapper.document;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.restdocs.cli.CliDocumentation.curlRequest;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.http.HttpDocumentation.httpRequest;
import static org.springframework.restdocs.http.HttpDocumentation.httpResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration;

@ExtendWith({RestDocumentationExtension.class})
class IncomeHandlerTest {

    private WebTestClient testClient;

    private InMemoryIncomeRepository incomeRepository = new InMemoryIncomeRepository();

    private IncomeHandler incomeHandler = new IncomeHandler(this.incomeRepository);

    private List<Income> fixtures = Arrays.asList(
        new IncomeBuilder()
            .withIncomeId(1)
            .withIncomeName("給与")
            .withAmount(200000)
            .withIncomeDate(LocalDate.of(2019, 4, 15))
            .build(),
        new IncomeBuilder()
            .withIncomeId(2)
            .withIncomeName("ボーナス")
            .withAmount(150000)
            .withIncomeDate(LocalDate.of(2019, 4, 25))
            .build());

    @BeforeEach
    void reset(RestDocumentationContextProvider restDocumentation) {
        this.testClient = WebTestClient.bindToRouterFunction(this.incomeHandler.routes())
            .handlerStrategies(App.handlerStrategies())
            .configureClient()
            .filter(documentationConfiguration(restDocumentation)
                .operationPreprocessors()
                .withRequestDefaults(prettyPrint())
                .withResponseDefaults(prettyPrint())
                .and()
                .snippets().withDefaults(httpRequest(), httpResponse(), curlRequest()))
            .build();
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
            })
            .consumeWith(
                document("get-incomes",
                    responseHeaders(headerWithName(CONTENT_TYPE).description(APPLICATION_JSON_VALUE)),
                    responseFields(
                        fieldWithPath("[].incomeId").type(JsonFieldType.NUMBER).description("The ID of the income"),
                        fieldWithPath("[].incomeName").type(JsonFieldType.STRING).description("The name of the income"),
                        fieldWithPath("[].amount").type(JsonFieldType.NUMBER).description("The amount price of the income"),
                        fieldWithPath("[].incomeDate").type(JsonFieldType.STRING).description("The date of the income"))));
    }

    @Test
    void get_200() {
        this.testClient.get()
            .uri("/incomes/{incomeId}", 1)
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
            })
            .consumeWith(
                document("get-income",
                    pathParameters(parameterWithName("incomeId").description("The ID of the income")),
                    responseHeaders(headerWithName(CONTENT_TYPE).description(APPLICATION_JSON_VALUE)),
                    responseFields(
                        fieldWithPath("incomeId").type(JsonFieldType.NUMBER).description("The ID of the income"),
                        fieldWithPath("incomeName").type(JsonFieldType.STRING).description("The name of the income"),
                        fieldWithPath("amount").type(JsonFieldType.NUMBER).description("The amount of the income"),
                        fieldWithPath("incomeDate").type(JsonFieldType.STRING).description("The date of the income"))));
    }

    @Test
    void get_404() {
        this.testClient.get()
            .uri("/incomes/{incomeId}", 10000)
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
    void post_201() {
        Map<String, Object> income = new LinkedHashMap<String, Object>() {

            {
                put("incomeName", "臨時収入");
                put("amount", 250000);
                put("incomeDate", "2019-04-28");
            }
        };
        this.testClient.post()
            .uri("/incomes")
            .bodyValue(income)
            .exchange()
            .expectStatus().isCreated()
            .expectBody(JsonNode.class)
            .consumeWith(result -> {
                URI location = result.getResponseHeaders().getLocation();
                assertThat(location.toString()).endsWith("/incomes/100");
                JsonNode body = result.getResponseBody();
                assertThat(body).isNotNull();

                assertThat(body.get("incomeId").asInt()).isEqualTo(100);
                assertThat(body.get("amount").asInt()).isEqualTo(250000);
                assertThat(body.get("incomeDate").asText()).isEqualTo("2019-04-28");
            })
            .consumeWith(
                document("post-incomes",
                    requestHeaders(headerWithName(CONTENT_TYPE).description(APPLICATION_JSON_VALUE)),
                    requestFields(
                        fieldWithPath("incomeName").type(JsonFieldType.STRING).description("The name of the income"),
                        fieldWithPath("amount").type(JsonFieldType.NUMBER).description("The amount of the income"),
                        fieldWithPath("incomeDate").type(JsonFieldType.STRING).description("The date of the income")),
                    responseHeaders(
                        headerWithName(CONTENT_TYPE).description(APPLICATION_JSON_VALUE),
                        headerWithName(LOCATION).description("The URL of the income")),
                    responseFields(
                        fieldWithPath("incomeId").type(JsonFieldType.NUMBER).description("The ID of the income"),
                        fieldWithPath("incomeName").type(JsonFieldType.STRING).description("The name of the income"),
                        fieldWithPath("amount").type(JsonFieldType.NUMBER).description("The amount of the income"),
                        fieldWithPath("incomeDate").type(JsonFieldType.STRING).description("The date of the income"))));

        this.testClient.get()
            .uri("/incomes/{incomeId}", 100)
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
    void post_400() {
        Map<String, Object> income = new LinkedHashMap<String, Object>() {

            {
                put("incomeId", 1000);
                put("incomeName", "");
                put("amount", -1);
            }
        };

        this.testClient.post()
            .uri("/incomes")
            .bodyValue(income)
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody(JsonNode.class)
            .consumeWith(result -> {
                JsonNode body = result.getResponseBody();
                assertThat(body).isNotNull();

                assertThat(body.get("status").asInt()).isEqualTo(400);
                assertThat(body.get("error").asText()).isEqualTo("Bad Request");
                assertThat(body.get("details").size()).isEqualTo(4);
                assertThat(body.get("details").get("incomeId").get(0).asText()).isEqualTo("\"incomeId\" must be null");
                assertThat(body.get("details").get("incomeName").get(0).asText()).isEqualTo("\"incomeName\" must not be empty");
                assertThat(body.get("details").get("amount").get(0).asText()).isEqualTo("\"amount\" must be greater than 0");
                assertThat(body.get("details").get("incomeDate").get(0).asText()).isEqualTo("\"incomeDate\" must not be null");
            });
    }

    @Test
    void delete() {
        this.testClient.delete()
            .uri("/incomes/{incomeId}", 1)
            .exchange()
            .expectStatus().isNoContent()
            .expectBody(Void.class)
            .consumeWith(
                document("delete-income",
                    pathParameters(parameterWithName("incomeId").description("The ID of the income"))));

        this.testClient.get()
            .uri("/incomes/1")
            .exchange()
            .expectStatus().isNotFound();
    }
}