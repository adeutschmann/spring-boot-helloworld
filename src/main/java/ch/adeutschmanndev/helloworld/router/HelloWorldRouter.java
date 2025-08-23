package ch.adeutschmanndev.helloworld.router;

import ch.adeutschmanndev.helloworld.resource.HelloWorldHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.web.servlet.function.RequestPredicates.GET;
import static org.springframework.web.servlet.function.RequestPredicates.accept;

@Configuration
public class HelloWorldRouter {

    @Bean
    public RouterFunction<ServerResponse> helloWorldRoutes(HelloWorldHandler handler) {
        return RouterFunctions
                .route(GET("/hello").and(accept(MediaType.APPLICATION_JSON)), handler::hello);
    }
}
