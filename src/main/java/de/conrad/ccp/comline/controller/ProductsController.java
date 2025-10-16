package de.conrad.ccp.comline.controller;

import de.conrad.ccp.comline.api.model.Product;
import de.conrad.ccp.comline.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductsController {

    private final ProductService productService;

    @GetMapping(value = "/{ctoNr}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Product> getProductByCtoNr(
            @PathVariable("ctoNr") String ctoNr,
            @RequestParam("accessToken") String accessToken) {
        log.info("Received request to get product by CTO Number: {}", ctoNr);
        return productService.getProductByCtoNr(ctoNr, accessToken)
                .doOnSuccess(product -> log.info("Successfully retrieved product with CTO Number: {}", ctoNr))
                .doOnError(error -> log.error("Error retrieving product with CTO Number: {}", ctoNr, error));
    }
}
