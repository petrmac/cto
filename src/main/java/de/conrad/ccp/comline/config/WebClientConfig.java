package de.conrad.ccp.comline.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.resolver.DefaultAddressResolverGroup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * WebClient configuration for HTTP client settings.
 * <p>
 * Configures connection pooling, timeouts, and DNS resolution behavior
 * for the reactive HTTP client used to call external APIs.
 *
 * @see WebClientProperties
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(WebClientProperties.class)
public class WebClientConfig {

    private final WebClientProperties properties;

    /**
     * Creates a configured WebClient bean with custom timeouts and DNS resolver settings.
     * <p>
     * Configuration includes:
     * <ul>
     *   <li>Connection pooling with configurable max connections</li>
     *   <li>Connection, read, and write timeouts</li>
     *   <li>Optional JVM DNS resolver for corporate security compatibility</li>
     * </ul>
     *
     * @param builder the WebClient builder
     * @return configured WebClient instance
     */
    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        // Connection pool configuration
        ConnectionProvider connectionProvider = ConnectionProvider.builder("comline-connection-pool")
                .maxConnections(properties.getMax().getConnections())
                .pendingAcquireTimeout(Duration.ofMillis(properties.getPending().getAcquire().getTimeout()))
                .build();

        // HTTP client with timeout configuration
        HttpClient httpClient = HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.getConnection().getTimeout())
                .responseTimeout(Duration.ofMillis(properties.getRead().getTimeout()))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(properties.getRead().getTimeout(), TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(properties.getWrite().getTimeout(), TimeUnit.MILLISECONDS))
                );

        // DNS resolver configuration
        if (properties.getUseJvmDnsResolver()) {
            log.info("Using JVM DNS resolver (workaround for corporate security software like Palo Alto)");
            httpClient = httpClient.resolver(DefaultAddressResolverGroup.INSTANCE);
        } else {
            log.info("Using Netty's default DNS resolver");
        }

        log.debug("WebClient configuration: connectionTimeout={}ms, readTimeout={}ms, writeTimeout={}ms, maxConnections={}, pendingAcquireTimeout={}ms",
                properties.getConnection().getTimeout(),
                properties.getRead().getTimeout(),
                properties.getWrite().getTimeout(),
                properties.getMax().getConnections(),
                properties.getPending().getAcquire().getTimeout());

        return builder
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
