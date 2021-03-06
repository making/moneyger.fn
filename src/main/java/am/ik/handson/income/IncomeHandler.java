package am.ik.handson.income;

import am.ik.handson.error.ErrorResponseBuilder;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

public class IncomeHandler {

    private final IncomeRepository incomeRepository;

    public IncomeHandler(IncomeRepository incomeRepository) {
        this.incomeRepository = incomeRepository;
    }

    public RouterFunction<ServerResponse> routes() {
        return RouterFunctions.route()
            .path("/incomes", b -> b
                .GET("/", this::list)
                .POST("/", this::post)
                .GET("/{incomeId}", this::get)
                .DELETE("/{incomeId}", this::delete))
            .build();
    }

    Mono<ServerResponse> list(ServerRequest req) {
        return ServerResponse.ok().body(this.incomeRepository.findAll(), Income.class);
    }

    Mono<ServerResponse> post(ServerRequest req) {
        return req.bodyToMono(Income.class)
            .flatMap(income -> income.validate()
                .bimap(v -> new ErrorResponseBuilder().withStatus(BAD_REQUEST).withDetails(v).build(), this.incomeRepository::save)
                .fold(error -> ServerResponse.badRequest().bodyValue(error),
                    result -> result.flatMap(created -> ServerResponse
                        .created(UriComponentsBuilder.fromUri(req.uri()).path("/{incomeId}").build(created.getIncomeId()))
                        .bodyValue(created))));
    }

    Mono<ServerResponse> get(ServerRequest req) {
        return this.incomeRepository.findById(Integer.valueOf(req.pathVariable("incomeId")))
            .flatMap(income -> ServerResponse.ok().bodyValue(income))
            .switchIfEmpty(ServerResponse.status(NOT_FOUND)
                .bodyValue(new ErrorResponseBuilder()
                    .withMessage("The given income is not found.")
                    .withStatus(NOT_FOUND)
                    .build()));
    }

    Mono<ServerResponse> delete(ServerRequest req) {
        return ServerResponse.noContent()
            .build(this.incomeRepository.deleteById(Integer.valueOf(req.pathVariable("incomeId"))));
    }
}
