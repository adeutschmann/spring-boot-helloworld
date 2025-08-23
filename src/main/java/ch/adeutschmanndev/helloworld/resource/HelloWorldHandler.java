package ch.adeutschmanndev.helloworld.resource;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.Map;

@Component
public class HelloWorldHandler {

    public ServerResponse hello(ServerRequest request) {
        Map<String, String> response = Map.of("message", "Hello World");
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }
}
