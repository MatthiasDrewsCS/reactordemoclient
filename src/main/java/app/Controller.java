package app;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@RestController
public class Controller {

    private static final String REMOTE_URL = "http://localhost:8080/get";
    private static final int CALL_COUNT = 10;

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    @Autowired
    private WebClient.Builder webClientBuilder;

    private RestTemplate restTemplate;
    private WebClient webClient;

    @PostConstruct
    public void init() {
        restTemplate = restTemplateBuilder.build();
        webClient = webClientBuilder.baseUrl(REMOTE_URL).build();
    }

    @GetMapping("resttemplate")
    public Iterable<String> getRestTemplate() {
        List<String> results = new ArrayList<>();
        for (int i = 0; i < CALL_COUNT; i++) {
            results.add(restTemplate.getForObject(REMOTE_URL, String.class));
        }
        return results;
    }

    @GetMapping("resttemplate-reactor")
    public Flux<String> getRestTemplateReactor() {
        return Flux.range(1, CALL_COUNT)
                .flatMap(val -> Mono.fromCallable(() -> restTemplate.getForObject(REMOTE_URL, String.class))
                        .subscribeOn(Schedulers.elastic()));
    }

    @GetMapping("webclient")
    public Flux<String> getWebClient() {
        return Flux.range(1, CALL_COUNT)
                .flatMap(val -> webClient
                        .get()
                        .retrieve()
                        .bodyToMono(String.class));
    }

    @GetMapping("webclient-block")
    public Iterable<String> getWebClientBlock() {
        return Flux.range(1, CALL_COUNT)
                .flatMap(val -> webClient
                        .get()
                        .retrieve()
                        .bodyToMono(String.class))
                .collectList()
                .block();
    }

    @GetMapping("webclient-reactor")
    public Flux<String> getWebClientReactor() {
        return Flux.range(1, CALL_COUNT)
                .flatMap(val -> webClient
                        .get()
                        .retrieve()
                        .bodyToMono(String.class)
                        .subscribeOn(Schedulers.elastic()));
    }

    @GetMapping(value = "metrics.stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> getMetricsStream() {
        return Flux.interval(Duration.ofSeconds(5))
                .map(val -> "jvm.threads.live " + meterRegistry.find("jvm.threads.live").gauge().value());
    }
}