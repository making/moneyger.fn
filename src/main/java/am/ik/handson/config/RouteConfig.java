package am.ik.handson.config;

import am.ik.handson.error.ErrorResponseExceptionHandler;
import am.ik.handson.expenditure.ExpenditureHandler;
import am.ik.handson.income.IncomeHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.CacheControl;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.time.Duration;

@Configuration
public class RouteConfig {

    public static HandlerStrategies handlerStrategies() {
        return HandlerStrategies.empty()
            .codecs(configure -> {
                configure.registerDefaults(true);
                ServerCodecConfigurer.ServerDefaultCodecs defaults = configure
                    .defaultCodecs();
                ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json()
                    .dateFormat(new StdDateFormat())
                    .build();
                defaults.jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper));
                defaults.jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper));
            })
            .exceptionHandler(new ErrorResponseExceptionHandler())
            .build();
    }

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
