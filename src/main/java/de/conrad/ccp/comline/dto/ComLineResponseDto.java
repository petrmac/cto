package de.conrad.ccp.comline.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * ComLine Response DTO - represents the full ComLine API response
 */
public record ComLineResponseDto(
        @JsonProperty("artikeldaten")
        ComLineProductDto artikeldaten,

        @JsonProperty("kundendaten")
        ComLineCustomerDataDto kundendaten,

        @JsonProperty("checksum")
        String checksum
) {
}
