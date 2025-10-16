package de.conrad.ccp.comline.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * OpenAPI configuration - uses the contract-first approach
 * The OpenAPI specification from comline-api.yaml is the source of truth
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() throws IOException {
        // Read the OpenAPI spec file from classpath
        ClassPathResource resource = new ClassPathResource("openapi/comline-api.yaml");
        String yamlContent = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        // Parse the OpenAPI spec
        SwaggerParseResult result = new OpenAPIV3Parser().readContents(yamlContent);

        if (result.getOpenAPI() == null) {
            throw new IllegalStateException("Failed to parse OpenAPI specification: " + result.getMessages());
        }

        return result.getOpenAPI();
    }
}
