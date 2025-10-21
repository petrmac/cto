package de.conrad.ccp.comline.service

import de.conrad.ccp.comline.api.model.Product
import de.conrad.ccp.comline.config.ComLineApiProperties
import de.conrad.ccp.comline.dto.ComLineProductDto
import de.conrad.ccp.comline.dto.ComLineResponseDto
import de.conrad.ccp.comline.mapper.ProductMapper
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.mapstruct.factory.Mappers
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification
import spock.lang.Subject

import java.time.OffsetDateTime

class ComlineProductServiceSpec extends Specification {

    WebClient webClient = Mock()
    WebClient.RequestHeadersUriSpec requestHeadersUriSpec = Mock()
    WebClient.RequestHeadersSpec requestHeadersSpec = Mock()
    WebClient.ResponseSpec responseSpec = Mock()
    ComLineApiProperties properties = Mock()
    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule())
    ProductMapper productMapper = Mappers.getMapper(ProductMapper.class)
    MeterRegistry meterRegistry = Mock()
    Counter successCounter = Mock()
    Counter failureCounter = Mock()

    @Subject
    ComlineProductService service

    def setup() {
        service = new ComlineProductService(webClient, properties, objectMapper, productMapper, meterRegistry)

        // Mock meter registry to return counters
        meterRegistry.counter(_ as String, _ as Iterable) >> successCounter
        Counter.builder("comline.api.calls") >> Mock(Counter.Builder) {
            description(_) >> it
            tag(_, _) >> it
            register(_) >> { args ->
                args[0] == meterRegistry ? successCounter : failureCounter
            }
        }

        service.initCounters()
        service.successCounter = successCounter
        service.failureCounter = failureCounter

        // Mock properties
        properties.getBaseUrl() >> "https://ctofinder.comline-shop.de/4DCGI/direct"
        properties.getMid() >> "219"
        properties.getAction() >> "getCTOConf"
        properties.getCustomerNumber() >> "15017319"
        properties.getPassword() >> "testPassword"
    }

    def "should successfully retrieve and map product by CTO number"() {
        given: "a valid CTO number and access token"
        def ctoNr = "CZ1FU-013020"
        def accessToken = "test-token"

        and: "a sample ComLine API response"
        def apiResponse = """
        {
          "artikeldaten": {
            "comline_artikelnummer": "CZ1FU-013020",
            "comline_artikelbezeichnung": "Apple MacBook Pro 16'' SpaceSchwarz CTO M4 Pro 14-Core CPU 20-Core GPU (48GB,4TB,britisch)",
            "comline_artikelbeschreibung": "CTO Variante der Grundkonfiguration MX2Y3D/A",
            "haendler_ek_netto": 3610.97,
            "pos_vk_brutto": 4899,
            "pos_vk": 4899,
            "uvp_netto": 4116.81,
            "hersteller_artikelnummer": "Z1FU-013020",
            "ean": "4069116466182",
            "artikel_laenge": 0.4,
            "artikel_breite": 0.075,
            "artikel_hoehe": 0.29,
            "gewicht_netto": 2.1,
            "gewicht_brutto": 3.5,
            "liefertermin": "2025-11-06T00:00:00Z",
            "url_bild": "//ezentrum-res.cloudinary.com/image/upload/v1730460185/comline/nj7tg26s7sjjny01rcfj.jpg",
            "referenz_artikel": ""
          },
          "kundendaten": {},
          "checksum": "test-checksum"
        }
        """

        and: "setup WebClient mocks to return API response"
        webClient.get() >> requestHeadersUriSpec
        requestHeadersUriSpec.uri(_ as String) >> requestHeadersSpec
        requestHeadersSpec.retrieve() >> responseSpec
        responseSpec.bodyToMono(String.class) >> Mono.just(apiResponse)

        when: "calling getProductByCtoNr"
        def result = service.getProductByCtoNr(ctoNr, accessToken)

        then: "result contains correct product data"
        StepVerifier.create(result)
                .expectNextMatches({ product ->
                    product.productIdentifier == ctoNr &&
                    product.name == "Apple MacBook Pro 16'' SpaceSchwarz CTO M4 Pro 14-Core CPU 20-Core GPU (48GB,4TB,britisch)" &&
                    product.ean == "4069116466182"
                })
                .verifyComplete()
    }

    def "should throw ProductNotFoundException when CTO number does not match"() {
        given: "a CTO number and access token"
        def ctoNr = "DIFFERENT-CTO"
        def accessToken = "test-token"

        and: "API response with different CTO number"
        def apiResponse = """
        {
          "artikeldaten": {
            "comline_artikelnummer": "CZ1FU-013020",
            "comline_artikelbezeichnung": "Test Product",
            "haendler_ek_netto": 100.0,
            "ean": "123456789"
          }
        }
        """

        when: "calling getProductByCtoNr"
        def result = service.getProductByCtoNr(ctoNr, accessToken)

        then: "WebClient is called"
        1 * webClient.get() >> requestHeadersUriSpec
        1 * requestHeadersUriSpec.uri(_ as String) >> requestHeadersSpec
        1 * requestHeadersSpec.retrieve() >> responseSpec
        1 * responseSpec.bodyToMono(String.class) >> Mono.just(apiResponse)

        and: "ProductNotFoundException is thrown"
        StepVerifier.create(result)
                .expectErrorMatches { it instanceof ProductService.ProductNotFoundException }
                .verify()
    }

    def "should handle missing artikeldaten in response"() {
        given: "a CTO number and access token"
        def ctoNr = "CZ1FU-013020"
        def accessToken = "test-token"

        and: "API response without artikeldaten"
        def apiResponse = """
        {
          "kundendaten": {},
          "checksum": "test-checksum"
        }
        """

        when: "calling getProductByCtoNr"
        def result = service.getProductByCtoNr(ctoNr, accessToken)

        then: "WebClient is called"
        1 * webClient.get() >> requestHeadersUriSpec
        1 * requestHeadersUriSpec.uri(_ as String) >> requestHeadersSpec
        1 * requestHeadersSpec.retrieve() >> responseSpec
        1 * responseSpec.bodyToMono(String.class) >> Mono.just(apiResponse)

        and: "RuntimeException is thrown"
        StepVerifier.create(result)
                .expectErrorMatches { it instanceof RuntimeException && it.message.contains("Failed to parse product") }
                .verify()
    }

    def "should handle invalid JSON response"() {
        given: "a CTO number and access token"
        def ctoNr = "CZ1FU-013020"
        def accessToken = "test-token"

        and: "invalid JSON response"
        def apiResponse = "{ invalid json }"

        when: "calling getProductByCtoNr"
        def result = service.getProductByCtoNr(ctoNr, accessToken)

        then: "WebClient is called"
        1 * webClient.get() >> requestHeadersUriSpec
        1 * requestHeadersUriSpec.uri(_ as String) >> requestHeadersSpec
        1 * requestHeadersSpec.retrieve() >> responseSpec
        1 * responseSpec.bodyToMono(String.class) >> Mono.just(apiResponse)

        and: "RuntimeException is thrown"
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify()
    }

    def "should sanitize URL for logging"() {
        given: "a URL with sensitive data"
        def url = "https://api.example.com?pwd=secret&accesstoken=token123&other=param"

        when: "sanitizing the URL"
        def sanitized = service.sanitizeUrlForLogging(url)

        then: "sensitive data is masked"
        sanitized == "https://api.example.com?pwd=***&accesstoken=***&other=param"
    }
}
