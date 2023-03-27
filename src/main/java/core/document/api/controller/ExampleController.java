package core.document.api.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;

@RestController
public class ExampleController {

    @GetMapping(value = "/numbers",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Integer> getNumbers() {
        return Flux.range(1, 100).delayElements(Duration.ofSeconds(1));
    }
}