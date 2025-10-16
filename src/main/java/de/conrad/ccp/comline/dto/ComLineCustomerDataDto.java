package de.conrad.ccp.comline.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * ComLine Customer Data DTO - represents the "kundendaten" structure from ComLine API response
 */
public record ComLineCustomerDataDto(
        @JsonProperty("person_vorname")
        String personVorname,

        @JsonProperty("person_nachname")
        String personNachname,

        @JsonProperty("person_email")
        String personEmail,

        @JsonProperty("referenz_mid")
        String referenzMid,

        @JsonProperty("referenz_p2")
        String referenzP2,

        @JsonProperty("referenz_p3")
        String referenzP3,

        @JsonProperty("ctopos_titel")
        String ctoposTitel
) {
}
