package de.conrad.ccp.comline.controller

import de.conrad.ccp.comline.api.model.Product
import de.conrad.ccp.comline.service.ProductService
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification
import spock.lang.Subject

class ProductsControllerSpec extends Specification {

    ProductService productService = Mock()

    @Subject
    ProductsController controller

    def setup() {
        controller = new ProductsController(productService)
    }

    def "should successfully retrieve product by CTO number"() {
        given: "a valid CTO number and access token"
        def ctoNr = "CZ1FU-013020"
        def accessToken = "test-token"

        and: "a product from the service"
        def expectedProduct = Product.builder()
                .productIdentifier(ctoNr)
                .name("Apple MacBook Pro 16'' SpaceSchwarz CTO")
                .databasePrice(3610.97)
                .ean("4069116466182")
                .build()

        when: "calling getProductByCtoNr"
        def result = controller.getProductByCtoNr(ctoNr, accessToken)

        then: "service is called with correct parameters"
        1 * productService.getProductByCtoNr(ctoNr, accessToken) >> Mono.just(expectedProduct)

        and: "result is correct"
        StepVerifier.create(result)
                .expectNext(expectedProduct)
                .verifyComplete()
    }

    def "should propagate error from service"() {
        given: "a CTO number and access token"
        def ctoNr = "INVALID-CTO"
        def accessToken = "test-token"

        and: "service returns error"
        def error = new ProductService.ProductNotFoundException("Product not found")

        when: "calling getProductByCtoNr"
        def result = controller.getProductByCtoNr(ctoNr, accessToken)

        then: "service is called"
        1 * productService.getProductByCtoNr(ctoNr, accessToken) >> Mono.error(error)

        and: "error is propagated"
        StepVerifier.create(result)
                .expectError(ProductService.ProductNotFoundException.class)
                .verify()
    }

    def "should handle various CTO number formats"() {
        given: "different CTO number formats"
        def accessToken = "test-token"

        when: "calling with each CTO number"
        def result = controller.getProductByCtoNr(ctoNr, accessToken)

        then: "service is called with the CTO number"
        1 * productService.getProductByCtoNr(ctoNr, accessToken) >> Mono.just(Product.builder().productIdentifier(ctoNr).build())

        and: "result contains the CTO number"
        StepVerifier.create(result)
                .expectNextMatches { it.productIdentifier == ctoNr }
                .verifyComplete()

        where:
        ctoNr << ["CZ1FU-013020", "ABC123-XYZ", "TEST-001", "12345"]
    }
}
