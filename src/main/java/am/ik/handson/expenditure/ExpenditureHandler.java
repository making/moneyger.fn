package am.ik.handson.expenditure;

import am.ik.handson.error.ErrorResponseBuilder;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

public class ExpenditureHandler {

    private final ExpenditureRepository expenditureRepository;

    public ExpenditureHandler(ExpenditureRepository expenditureRepository) {
        this.expenditureRepository = expenditureRepository;
    }

    public RouterFunction<ServerResponse> routes() {
        return RouterFunctions.route()
            .path("/expenditures", b -> b
                .GET("/", this::list)
                .POST("/", this::post)
                .GET("/{expenditureId}", this::get)
                .DELETE("/{expenditureId}", this::delete))
            .build();
    }

    Mono<ServerResponse> list(ServerRequest req) {
        return ServerResponse.ok().body(this.expenditureRepository.findAll(), Expenditure.class);
    }

    Mono<ServerResponse> post(ServerRequest req) {
        return req.bodyToMono(Expenditure.class)
            .flatMap(body -> body.validate()
                .leftMap(v -> new ErrorResponseBuilder().withStatus(BAD_REQUEST).withDetails(v).createErrorResponse())
                .fold(error -> ServerResponse.badRequest().syncBody(error),
                    x -> this.expenditureRepository.save(body)
                        .flatMap(expenditure -> ServerResponse
                            .created(UriComponentsBuilder.fromUri(req.uri()).path("/{expenditureId}").build(expenditure.getExpenditureId()))
                            .syncBody(expenditure))));
    }

    Mono<ServerResponse> get(ServerRequest req) {
        return this.expenditureRepository.findById(Integer.valueOf(req.pathVariable("expenditureId")))
            .flatMap(expenditure -> ServerResponse.ok().syncBody(expenditure))
            .switchIfEmpty(ServerResponse.status(NOT_FOUND)
                .syncBody(new ErrorResponseBuilder()
                    .withMessage("The given expenditure is not found.")
                    .withStatus(NOT_FOUND)
                    .createErrorResponse()));
    }

    Mono<ServerResponse> delete(ServerRequest req) {
        return ServerResponse.noContent()
            .build(this.expenditureRepository.deleteById(Integer.valueOf(req.pathVariable("expenditureId"))));
    }
}
