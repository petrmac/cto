package de.conrad.ccp.comline.config

import org.springframework.web.reactive.function.client.WebClient
import spock.lang.Specification

/**
 * Test specification for WebClientConfig configuration class.
 */
class WebClientConfigSpec extends Specification {

    WebClientConfig config
    WebClientProperties properties

    def setup() {
        properties = new WebClientProperties()
        config = new WebClientConfig(properties)
    }

    def "should create WebClient with default properties"() {
        given: "a WebClient builder"
        def builder = WebClient.builder()

        when: "creating WebClient"
        def webClient = config.webClient(builder)

        then: "WebClient is created successfully"
        webClient != null
    }

    def "should create WebClient with custom connection timeout"() {
        given: "a WebClient builder and custom connection timeout"
        def builder = WebClient.builder()
        properties.connection.timeout = 5000

        when: "creating WebClient"
        def webClient = config.webClient(builder)

        then: "WebClient is created successfully"
        webClient != null
    }

    def "should create WebClient with custom read timeout"() {
        given: "a WebClient builder and custom read timeout"
        def builder = WebClient.builder()
        properties.read.timeout = 60000

        when: "creating WebClient"
        def webClient = config.webClient(builder)

        then: "WebClient is created successfully"
        webClient != null
    }

    def "should create WebClient with custom write timeout"() {
        given: "a WebClient builder and custom write timeout"
        def builder = WebClient.builder()
        properties.write.timeout = 15000

        when: "creating WebClient"
        def webClient = config.webClient(builder)

        then: "WebClient is created successfully"
        webClient != null
    }

    def "should create WebClient with custom max connections"() {
        given: "a WebClient builder and custom max connections"
        def builder = WebClient.builder()
        properties.max.connections = 200

        when: "creating WebClient"
        def webClient = config.webClient(builder)

        then: "WebClient is created successfully"
        webClient != null
    }

    def "should create WebClient with custom pending acquire timeout"() {
        given: "a WebClient builder and custom pending acquire timeout"
        def builder = WebClient.builder()
        properties.pending.acquire.timeout = 60000

        when: "creating WebClient"
        def webClient = config.webClient(builder)

        then: "WebClient is created successfully"
        webClient != null
    }

    def "should create WebClient with JVM DNS resolver enabled"() {
        given: "a WebClient builder with JVM DNS resolver enabled"
        def builder = WebClient.builder()
        properties.useJvmDnsResolver = true

        when: "creating WebClient"
        def webClient = config.webClient(builder)

        then: "WebClient is created successfully"
        webClient != null
    }

    def "should create WebClient with JVM DNS resolver disabled"() {
        given: "a WebClient builder with JVM DNS resolver disabled"
        def builder = WebClient.builder()
        properties.useJvmDnsResolver = false

        when: "creating WebClient"
        def webClient = config.webClient(builder)

        then: "WebClient is created successfully"
        webClient != null
    }

    def "should create WebClient with all custom properties"() {
        given: "a WebClient builder with all custom properties"
        def builder = WebClient.builder()
        properties.connection.timeout = 8000
        properties.read.timeout = 45000
        properties.write.timeout = 12000
        properties.max.connections = 150
        properties.pending.acquire.timeout = 50000
        properties.useJvmDnsResolver = true

        when: "creating WebClient"
        def webClient = config.webClient(builder)

        then: "WebClient is created successfully"
        webClient != null
    }

    def "should handle minimum timeout values"() {
        given: "a WebClient builder with minimum timeout values"
        def builder = WebClient.builder()
        properties.connection.timeout = 1
        properties.read.timeout = 1
        properties.write.timeout = 1

        when: "creating WebClient"
        def webClient = config.webClient(builder)

        then: "WebClient is created successfully"
        webClient != null
    }

    def "should handle maximum timeout values"() {
        given: "a WebClient builder with large timeout values"
        def builder = WebClient.builder()
        properties.connection.timeout = 120000
        properties.read.timeout = 300000
        properties.write.timeout = 120000

        when: "creating WebClient"
        def webClient = config.webClient(builder)

        then: "WebClient is created successfully"
        webClient != null
    }

    def "should create independent WebClient instances"() {
        given: "a WebClient builder"
        def builder1 = WebClient.builder()
        def builder2 = WebClient.builder()

        when: "creating two WebClient instances"
        def webClient1 = config.webClient(builder1)
        def webClient2 = config.webClient(builder2)

        then: "both instances are created"
        webClient1 != null
        webClient2 != null
        // Note: We can't easily compare if they're different instances
        // because WebClient doesn't implement equals/hashCode
    }
}
