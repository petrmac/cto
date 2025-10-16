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
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

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
        log.info("Calling ComLine API for product with CTO Number {}: {}", ctoNr, sanitizeUrlForLogging(url));

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(response -> {
                    try {
                        log.debug("Received response from ComLine API");
                        ComLineProductDto comLineProduct = parseComLineProduct(response);

                        // Verify that the returned product matches the requested CTO Number
                        if (!ctoNr.equals(comLineProduct.comlineArtikelnummer())) {
                            log.warn("Requested CTO Number {} does not match returned product CTO Number {}", ctoNr, comLineProduct.comlineArtikelnummer());
                            return Mono.error(new ProductNotFoundException("Product with CTO Number " + ctoNr + " not found"));
                        }

                        Product product = productMapper.toProduct(comLineProduct);

                        // Increment success counter
                        successCounter.increment();

                        log.info("Successfully retrieved and mapped product with CTO Number: {}", ctoNr);
                        return Mono.just(product);
                    } catch (Exception e) {
                        log.error("Error parsing product from ComLine API for CTO Number: {}", ctoNr, e);
                        return Mono.error(new RuntimeException("Failed to parse product from ComLine API", e));
                    }
                })
                .doOnError(error -> {
                    // Increment failure counter for all errors
                    failureCounter.increment();
                    log.error("Error retrieving product from ComLine API for CTO Number: {}", ctoNr, error);
                });
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
