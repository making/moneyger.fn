package am.ik.handson.config;

import am.ik.handson.expenditure.ExpenditureHandler;
import am.ik.handson.income.IncomeHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.CacheControl;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.time.Duration;

@Configuration
public class RouteConfig {

    @Bean
    public RouterFunction<ServerResponse> routes(ExpenditureHandler expenditureHandler, IncomeHandler incomeHandler) {
        return staticRoutes()
            .and(expenditureHandler.routes())
            .and(incomeHandler.routes());
    }

    static RouterFunction<ServerResponse> staticRoutes() {
        return RouterFunctions.route()
            .GET("/", req -> ServerResponse.ok().bodyValue(new ClassPathResource("META-INF/resources/index.html")))
            .resources("/docs/**", new ClassPathResource("static/docs/"))
            .resources("/webjars/**", new ClassPathResource("META-INF/resources/webjars/"))
            .resources("/**", new ClassPathResource("META-INF/resources/"))
            .filter((request, next) -> next.handle(request)
                .flatMap(response -> ServerResponse.from(response)
                    .cacheControl(CacheControl.maxAge(Duration.ofDays(3)))
                    .build(response::writeTo)))
            .build();
    }
}
