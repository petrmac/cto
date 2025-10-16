package de.conrad.ccp.comline.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "comline.api")
public class ComLineApiProperties {

    private String baseUrl;
    private String mid;
    private String action;
    private String customerNumber;
    private String password;
}
