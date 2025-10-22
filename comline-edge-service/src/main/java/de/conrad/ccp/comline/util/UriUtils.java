package de.conrad.ccp.comline.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.Optional;

/**
 * Utility class for URI/URL operations
 */
@UtilityClass
@Slf4j
public class UriUtils {

    /**
     * Converts a string URL to a URI, handling protocol-relative URLs
     * and invalid formats gracefully.
     * <p>
     * Features:
     * - Returns empty Optional for null, blank, or invalid URLs
     * - Automatically adds 'https:' prefix for protocol-relative URLs (starting with '//')
     * - Preserves absolute URLs (http://, https://) as-is
     * - Logs conversion details and errors
     *
     * @param url the URL string to convert
     * @return Optional containing the URI if valid, empty otherwise
     */
    public static Optional<URI> stringToUri(String url) {
        if (url == null || url.isBlank()) {
            log.trace("URL is null or blank, returning empty Optional");
            return Optional.empty();
        }

        String originalUrl = url;
        try {
            // Handle protocol-relative URLs (//domain.com/path)
            if (url.startsWith("//")) {
                url = "https:" + url;
                log.debug("Converted protocol-relative URL: {} -> {}", originalUrl, url);
            }

            URI uri = URI.create(url);
            log.trace("Successfully converted URL to URI: {}", uri);
            return Optional.of(uri);

        } catch (IllegalArgumentException e) {
            log.warn("Failed to convert invalid URL to URI: '{}'. Error: {}", originalUrl, e.getMessage());
            return Optional.empty();
        }
    }
}
