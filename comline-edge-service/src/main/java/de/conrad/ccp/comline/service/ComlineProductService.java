package de.conrad.ccp.comline.service;

import de.conrad.ccp.comline.api.model.Product;
import de.conrad.ccp.comline.config.ComLineApiProperties;
import de.conrad.ccp.comline.dto.ComLineProductDto;
import de.conrad.ccp.comline.dto.ComLineResponseDto;
import de.conrad.ccp.comline.mapper.ProductMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComlineProductService implements ProductService {

    private final WebClient webClient;
    private final ComLineApiProperties properties;
    private final ObjectMapper objectMapper;
    private final ProductMapper productMapper;
    private final MeterRegistry meterRegistry;
    private Counter successCounter;
    private Counter failureCounter;

    @PostConstruct
    public void initCounters() {
        // Initialize counters for ComLine API calls
        this.successCounter = Counter.builder("comline.api.calls")
                .description("Number of successful ComLine API calls")
                .tag("result", "success")
                .register(meterRegistry);

        this.failureCounter = Counter.builder("comline.api.calls")
                .description("Number of failed ComLine API calls")
                .tag("result", "failure")
                .register(meterRegistry);
    }

    @Timed(value = "comline.api.get.product.by.cto.nr", description = "Time taken to fetch product by CTO Number from ComLine API")
    public Mono<Product> getProductByCtoNr(String ctoNr, String accessToken) {
        String url = buildUrl(ctoNr, accessToken);
        long startTime = System.currentTimeMillis();

        log.info("Calling ComLine API for product {}, {}",
                kv("ctoNr", ctoNr),
                kv("url", sanitizeUrlForLogging(url)));

        return webClient.get()
                .uri(url)
                .exchangeToMono(response -> handleResponse(response, ctoNr, startTime))
                .onErrorResume(error -> handleError(error, ctoNr, startTime));
    }

    private Mono<Product> handleResponse(ClientResponse response, String ctoNr, long startTime) {
        HttpStatusCode statusCode = response.statusCode();
        long duration = System.currentTimeMillis() - startTime;

        if (statusCode.is2xxSuccessful()) {
            log.info("ComLine API call successful {}, {}, {}",
                    kv("ctoNr", ctoNr),
                    kv("http_status", statusCode.value()),
                    kv("duration_ms", duration));

            return response.bodyToMono(String.class)
                    .flatMap(responseBody -> parseAndMapProduct(responseBody, ctoNr))
                    .doOnSuccess(product -> successCounter.increment());

        } else if (statusCode.is4xxClientError()) {
            log.warn("ComLine API returned client error {}, {}, {}",
                    kv("ctoNr", ctoNr),
                    kv("http_status", statusCode.value()),
                    kv("duration_ms", duration));

            return response.bodyToMono(String.class)
                    .doOnNext(errorBody -> log.warn("ComLine API error response body: {}", errorBody))
                    .flatMap(errorBody -> {
                        failureCounter.increment();
                        return Mono.<Product>error(new ProductNotFoundException(
                                "Product with CTO Number " + ctoNr + " not found (HTTP " + statusCode.value() + ")"));
                    })
                    .switchIfEmpty(Mono.defer(() -> {
                        failureCounter.increment();
                        return Mono.error(new ProductNotFoundException(
                                "Product with CTO Number " + ctoNr + " not found (HTTP " + statusCode.value() + ")"));
                    }));

        } else if (statusCode.is5xxServerError()) {
            log.error("ComLine API returned server error {}, {}, {}",
                    kv("ctoNr", ctoNr),
                    kv("http_status", statusCode.value()),
                    kv("duration_ms", duration));

            return response.bodyToMono(String.class)
                    .doOnNext(errorBody -> log.error("ComLine API server error response body: {}", errorBody))
                    .flatMap(errorBody -> {
                        failureCounter.increment();
                        return Mono.<Product>error(new RuntimeException(
                                "ComLine API server error (HTTP " + statusCode.value() + ")"));
                    })
                    .switchIfEmpty(Mono.defer(() -> {
                        failureCounter.increment();
                        return Mono.error(new RuntimeException(
                                "ComLine API server error (HTTP " + statusCode.value() + ")"));
                    }));

        } else {
            log.warn("ComLine API returned unexpected status code {}, {}, {}",
                    kv("ctoNr", ctoNr),
                    kv("http_status", statusCode.value()),
                    kv("duration_ms", duration));

            failureCounter.increment();
            return Mono.error(new RuntimeException(
                    "Unexpected HTTP status from ComLine API: " + statusCode.value()));
        }
    }

    private Mono<Product> parseAndMapProduct(String responseBody, String ctoNr) {
        try {
            log.debug("Parsing ComLine API response for CTO Number {}", ctoNr);
            ComLineProductDto comLineProduct = parseComLineProduct(responseBody);

            // Verify that the returned product matches the requested CTO Number
            if (!ctoNr.equals(comLineProduct.comlineArtikelnummer())) {
                log.warn("CTO Number mismatch {}, {}",
                        kv("requested", ctoNr),
                        kv("returned", comLineProduct.comlineArtikelnummer()));
                return Mono.error(new ProductNotFoundException(
                        "Product with CTO Number " + ctoNr + " not found"));
            }

            Product product = productMapper.toProduct(comLineProduct);
            log.info("Successfully mapped product {}", kv("ctoNr", ctoNr));
            return Mono.just(product);

        } catch (Exception e) {
            log.error("Error parsing product from ComLine API response {}, {}",
                    kv("ctoNr", ctoNr),
                    kv("error", e.getMessage()),
                    e);
            return Mono.error(new RuntimeException("Failed to parse product from ComLine API", e));
        }
    }

    private Mono<Product> handleError(Throwable error, String ctoNr, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        failureCounter.increment();

        String errorType = error.getClass().getSimpleName();
        log.error("Error calling ComLine API {}, {}, {}, {}",
                kv("ctoNr", ctoNr),
                kv("error_type", errorType),
                kv("error_message", error.getMessage()),
                kv("duration_ms", duration),
                error);

        return Mono.error(error);
    }

    private String buildUrl(String ctoNr, String accessToken) {
        return UriComponentsBuilder.fromHttpUrl(properties.getBaseUrl())
                .queryParam("mid", properties.getMid())
                .queryParam("action", properties.getAction())
                .queryParam("kdnr", properties.getCustomerNumber())
                .queryParam("pwd", properties.getPassword())
                .queryParam("accesstoken", accessToken)
                .queryParam("cto_nr", ctoNr)
                .toUriString();
    }

    private String sanitizeUrlForLogging(String url) {
        return url.replaceAll("(pwd=)[^&]*", "$1***")
                .replaceAll("(accesstoken=)[^&]*", "$1***");
    }

    private ComLineProductDto parseComLineProduct(String json) {
        try {
            ComLineResponseDto response = objectMapper.readValue(json, ComLineResponseDto.class);

            if (response.artikeldaten() == null) {
                log.error("No artikeldaten found in ComLine API response");
                throw new InvalidResponseException("Invalid response from ComLine API: missing artikeldaten");
            }

            // The ComLine API returns a single product in the "artikeldaten" field
            return response.artikeldaten();

        } catch (Exception e) {
            log.error("Error parsing product from JSON response", e);
            throw new InvalidResponseException("Failed to parse product from ComLine API response", e);
        }
    }
}
