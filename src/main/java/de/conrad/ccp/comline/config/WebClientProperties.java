package de.conrad.ccp.comline.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for WebClient HTTP client settings.
 * <p>
 * These properties control connection pooling, timeouts, and DNS resolution behavior.
 * All timeout values are in milliseconds.
 * <p>
 * Example usage in application.yml:
 * <pre>
 * webclient:
 *   connection:
 *     timeout: 10000
 *   read:
 *     timeout: 30000
 *   use-jvm-dns-resolver: true
 * </pre>
 */
@Data
@Validated
@ConfigurationProperties(prefix = "webclient")
public class WebClientProperties {

    /**
     * Connection timeout in milliseconds.
     * This is the maximum time to wait for a connection to be established.
     * Default: 10000ms (10 seconds)
     */
    @NotNull
    @Min(1)
    private Integer connectionTimeout = 10000;

    /**
     * Read timeout in milliseconds.
     * This is the maximum time to wait for reading data from the connection.
     * Default: 30000ms (30 seconds)
     */
    @NotNull
    @Min(1)
    private Integer readTimeout = 30000;

    /**
     * Write timeout in milliseconds.
     * This is the maximum time to wait for writing data to the connection.
     * Default: 10000ms (10 seconds)
     */
    @NotNull
    @Min(1)
    private Integer writeTimeout = 10000;

    /**
     * Maximum number of connections in the connection pool.
     * Default: 100
     */
    @NotNull
    @Min(1)
    private Integer maxConnections = 100;

    /**
     * Pending acquire timeout in milliseconds.
     * This is the maximum time to wait for acquiring a connection from the pool.
     * Default: 45000ms (45 seconds)
     */
    @NotNull
    @Min(1)
    private Integer pendingAcquireTimeout = 45000;

    /**
     * Use JVM DNS resolver instead of Netty's native DNS resolver.
     * <p>
     * Set this to {@code true} when experiencing DNS resolution issues with
     * corporate security software like Palo Alto GlobalProtect, Zscaler,
     * Cisco Umbrella, or other SSL-inspecting proxies.
     * <p>
     * When enabled, uses {@link io.netty.resolver.DefaultAddressResolverGroup}
     * which delegates to Java's built-in DNS resolver. This works better with
     * corporate DNS servers and security software that intercepts DNS queries.
     * <p>
     * Default: false (uses Netty's native DNS resolver)
     *
     * @see io.netty.resolver.DefaultAddressResolverGroup
     */
    @NotNull
    private Boolean useJvmDnsResolver = false;

    // Nested classes for better structure and YAML readability

    /**
     * Connection-related properties.
     */
    @Data
    public static class Connection {
        /**
         * Connection timeout in milliseconds.
         * Default: 10000ms (10 seconds)
         */
        @NotNull
        @Min(1)
        private Integer timeout = 10000;
    }

    /**
     * Read-related properties.
     */
    @Data
    public static class Read {
        /**
         * Read timeout in milliseconds.
         * Default: 30000ms (30 seconds)
         */
        @NotNull
        @Min(1)
        private Integer timeout = 30000;
    }

    /**
     * Write-related properties.
     */
    @Data
    public static class Write {
        /**
         * Write timeout in milliseconds.
         * Default: 10000ms (10 seconds)
         */
        @NotNull
        @Min(1)
        private Integer timeout = 10000;
    }

    /**
     * Maximum connections properties.
     */
    @Data
    public static class Max {
        /**
         * Maximum number of connections in the pool.
         * Default: 100
         */
        @NotNull
        @Min(1)
        private Integer connections = 100;
    }

    /**
     * Pending acquire properties.
     */
    @Data
    public static class Pending {
        /**
         * Acquire-related properties.
         */
        @Data
        public static class Acquire {
            /**
             * Pending acquire timeout in milliseconds.
             * Default: 45000ms (45 seconds)
             */
            @NotNull
            @Min(1)
            private Integer timeout = 45000;
        }

        private Acquire acquire = new Acquire();
    }

    // Nested property objects
    private Connection connection = new Connection();
    private Read read = new Read();
    private Write write = new Write();
    private Max max = new Max();
    private Pending pending = new Pending();
}
