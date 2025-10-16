package de.conrad.ccp.comline.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.resolver.DefaultAddressResolverGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class WebClientConfig {

    @Value("${webclient.connection.timeout:10000}")
    private int connectionTimeout;

    @Value("${webclient.read.timeout:30000}")
    private int readTimeout;

    @Value("${webclient.write.timeout:10000}")
    private int writeTimeout;

    @Value("${webclient.max.connections:100}")
    private int maxConnections;

    @Value("${webclient.pending.acquire.timeout:45000}")
    private int pendingAcquireTimeout;

    @Value("${webclient.use-jvm-dns-resolver:false}")
    private boolean useJvmDnsResolver;

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        ConnectionProvider connectionProvider = ConnectionProvider.builder("comline-connection-pool")
                .maxConnections(maxConnections)
                .pendingAcquireTimeout(Duration.ofMillis(pendingAcquireTimeout))
                .build();

        HttpClient httpClient = HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout)
                .responseTimeout(Duration.ofMillis(readTimeout))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(writeTimeout, TimeUnit.MILLISECONDS))
                );

        // Use JVM DNS resolver if configured (workaround for Palo Alto and corporate security)
        if (useJvmDnsResolver) {
            log.info("Using JVM DNS resolver (workaround for corporate security software like Palo Alto)");
            httpClient = httpClient.resolver(DefaultAddressResolverGroup.INSTANCE);
        } else {
            log.info("Using Netty's default DNS resolver");
        }

        return builder
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
