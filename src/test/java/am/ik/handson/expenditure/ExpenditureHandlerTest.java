package am.ik.handson.expenditure;

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
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration;

@ExtendWith({RestDocumentationExtension.class})
class ExpenditureHandlerTest {

    private WebTestClient testClient;

    private InMemoryExpenditureRepository expenditureRepository = new InMemoryExpenditureRepository();

    private ExpenditureHandler expenditureHandler = new ExpenditureHandler(this.expenditureRepository);

    private List<Expenditure> fixtures = Arrays.asList(
        new ExpenditureBuilder()
            .withExpenditureId(1)
            .withExpenditureName("本")
            .withUnitPrice(2000)
            .withQuantity(1)
            .withExpenditureDate(LocalDate.of(2019, 4, 1))
            .createExpenditure(),
        new ExpenditureBuilder()
            .withExpenditureId(2)
            .withExpenditureName("コーヒー")
            .withUnitPrice(300)
            .withQuantity(2)
            .withExpenditureDate(LocalDate.of(2019, 4, 2))
            .createExpenditure());

    @BeforeEach
    void reset(RestDocumentationContextProvider restDocumentation) {
        this.testClient = WebTestClient.bindToRouterFunction(this.expenditureHandler.routes())
            .handlerStrategies(App.handlerStrategies())
            .configureClient()
            .filter(documentationConfiguration(restDocumentation)
                .operationPreprocessors()
                .withRequestDefaults(prettyPrint())
                .withResponseDefaults(prettyPrint())
                .and()
                .snippets().withDefaults(httpRequest(), httpResponse(), curlRequest()))
            .build();
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
                assertThat(body.get(0).get("unitPrice").asInt()).isEqualTo(2000);
                assertThat(body.get(0).get("quantity").asInt()).isEqualTo(1);
                assertThat(body.get(0).get("expenditureDate").asText()).isEqualTo("2019-04-01");

                assertThat(body.get(1).get("expenditureId").asInt()).isEqualTo(2);
                assertThat(body.get(1).get("expenditureName").asText()).isEqualTo("コーヒー");
                assertThat(body.get(1).get("unitPrice").asInt()).isEqualTo(300);
                assertThat(body.get(1).get("quantity").asInt()).isEqualTo(2);
                assertThat(body.get(1).get("expenditureDate").asText()).isEqualTo("2019-04-02");
            })
            .consumeWith(
                document("get-expenditures",
                    responseHeaders(headerWithName(CONTENT_TYPE).description(APPLICATION_JSON_VALUE)),
                    responseFields(
                        fieldWithPath("[].expenditureId").type(JsonFieldType.NUMBER).description("The ID of the expenditure"),
                        fieldWithPath("[].expenditureName").type(JsonFieldType.STRING).description("The name of the expenditure"),
                        fieldWithPath("[].unitPrice").type(JsonFieldType.NUMBER).description("The unit price of the expenditure"),
                        fieldWithPath("[].quantity").type(JsonFieldType.NUMBER).description("The quantity of the expenditure"),
                        fieldWithPath("[].expenditureDate").type(JsonFieldType.STRING).description("The date of the expenditure"))));
    }

    @Test
    void get_200() {
        this.testClient.get()
            .uri("/expenditures/{expenditureId}", 1)
            .exchange()
            .expectStatus().isOk()
            .expectBody(JsonNode.class)
            .consumeWith(result -> {
                JsonNode body = result.getResponseBody();
                assertThat(body).isNotNull();

                assertThat(body.get("expenditureId").asInt()).isEqualTo(1);
                assertThat(body.get("expenditureName").asText()).isEqualTo("本");
                assertThat(body.get("unitPrice").asInt()).isEqualTo(2000);
                assertThat(body.get("quantity").asInt()).isEqualTo(1);
                assertThat(body.get("expenditureDate").asText()).isEqualTo("2019-04-01");
            })
            .consumeWith(
                document("get-expenditure",
                    pathParameters(parameterWithName("expenditureId").description("The ID of the expenditure")),
                    responseHeaders(headerWithName(CONTENT_TYPE).description(APPLICATION_JSON_VALUE)),
                    responseFields(
                        fieldWithPath("expenditureId").type(JsonFieldType.NUMBER).description("The ID of the expenditure"),
                        fieldWithPath("expenditureName").type(JsonFieldType.STRING).description("The name of the expenditure"),
                        fieldWithPath("unitPrice").type(JsonFieldType.NUMBER).description("The unit price of the expenditure"),
                        fieldWithPath("quantity").type(JsonFieldType.NUMBER).description("The quantity of the expenditure"),
                        fieldWithPath("expenditureDate").type(JsonFieldType.STRING).description("The date of the expenditure"))));
    }

    @Test
    void get_404() {
        this.testClient.get()
            .uri("/expenditures/{expenditureId}", 10000)
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
    void post_201() {
        Map<String, Object> expenditure = new LinkedHashMap<String, Object>() {

            {
                put("expenditureName", "ビール");
                put("unitPrice", 250);
                put("quantity", 1);
                put("expenditureDate", "2019-04-03");
            }
        };
        this.testClient.post()
            .uri("/expenditures")
            .bodyValue(expenditure)
            .exchange()
            .expectStatus().isCreated()
            .expectBody(JsonNode.class)
            .consumeWith(result -> {
                URI location = result.getResponseHeaders().getLocation();
                assertThat(location.toString()).endsWith("/expenditures/100");
                JsonNode body = result.getResponseBody();
                assertThat(body).isNotNull();

                assertThat(body.get("expenditureId").asInt()).isEqualTo(100);
                assertThat(body.get("expenditureName").asText()).isEqualTo("ビール");
                assertThat(body.get("unitPrice").asInt()).isEqualTo(250);
                assertThat(body.get("quantity").asInt()).isEqualTo(1);
                assertThat(body.get("expenditureDate").asText()).isEqualTo("2019-04-03");
            })
            .consumeWith(
                document("post-expenditures",
                    requestHeaders(headerWithName(CONTENT_TYPE).description(APPLICATION_JSON_VALUE)),
                    requestFields(
                        fieldWithPath("expenditureName").type(JsonFieldType.STRING).description("The name of the expenditure"),
                        fieldWithPath("unitPrice").type(JsonFieldType.NUMBER).description("The unit price of the expenditure"),
                        fieldWithPath("quantity").type(JsonFieldType.NUMBER).description("The quantity of the expenditure"),
                        fieldWithPath("expenditureDate").type(JsonFieldType.STRING).description("The date of the expenditure")),
                    responseHeaders(
                        headerWithName(CONTENT_TYPE).description(APPLICATION_JSON_VALUE),
                        headerWithName(LOCATION).description("The URL of the expenditure")),
                    responseFields(
                        fieldWithPath("expenditureId").type(JsonFieldType.NUMBER).description("The ID of the expenditure"),
                        fieldWithPath("expenditureName").type(JsonFieldType.STRING).description("The name of the expenditure"),
                        fieldWithPath("unitPrice").type(JsonFieldType.NUMBER).description("The unit price of the expenditure"),
                        fieldWithPath("quantity").type(JsonFieldType.NUMBER).description("The quantity of the expenditure"),
                        fieldWithPath("expenditureDate").type(JsonFieldType.STRING).description("The date of the expenditure"))));

        this.testClient.get()
            .uri("/expenditures/{expenditureId}", 100)
            .exchange()
            .expectStatus().isOk()
            .expectBody(JsonNode.class)
            .consumeWith(result -> {
                JsonNode body = result.getResponseBody();
                assertThat(body).isNotNull();

                assertThat(body.get("expenditureId").asInt()).isEqualTo(100);
                assertThat(body.get("expenditureName").asText()).isEqualTo("ビール");
                assertThat(body.get("unitPrice").asInt()).isEqualTo(250);
                assertThat(body.get("quantity").asInt()).isEqualTo(1);
                assertThat(body.get("expenditureDate").asText()).isEqualTo("2019-04-03");
            });
    }

    @Test
    void post_400() {
        Expenditure expenditure = new ExpenditureBuilder()
            .withExpenditureId(1000)
            .withExpenditureName("")
            .withUnitPrice(-1)
            .withQuantity(-1)
            .withExpenditureDate(null)
            .createExpenditure();

        this.testClient.post()
            .uri("/expenditures")
            .bodyValue(expenditure)
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody(JsonNode.class)
            .consumeWith(result -> {
                JsonNode body = result.getResponseBody();
                assertThat(body).isNotNull();

                assertThat(body.get("status").asInt()).isEqualTo(400);
                assertThat(body.get("error").asText()).isEqualTo("Bad Request");
                assertThat(body.get("details").size()).isEqualTo(5);
                assertThat(body.get("details").get("expenditureId").get(0).asText()).isEqualTo("\"expenditureId\" must be null");
                assertThat(body.get("details").get("expenditureName").get(0).asText()).isEqualTo("\"expenditureName\" must not be empty");
                assertThat(body.get("details").get("unitPrice").get(0).asText()).isEqualTo("\"unitPrice\" must be greater than 0");
                assertThat(body.get("details").get("quantity").get(0).asText()).isEqualTo("\"quantity\" must be greater than 0");
                assertThat(body.get("details").get("expenditureDate").get(0).asText()).isEqualTo("\"expenditureDate\" must not be null");
            });
    }

    @Test
    void delete() {
        this.testClient.delete()
            .uri("/expenditures/{expenditureId}", 1)
            .exchange()
            .expectStatus().isNoContent()
            .expectBody(Void.class)
            .consumeWith(
                document("delete-expenditure",
                    pathParameters(parameterWithName("expenditureId").description("The ID of the expenditure"))));

        this.testClient.get()
            .uri("/expenditures/1")
            .exchange()
            .expectStatus().isNotFound();
    }
}