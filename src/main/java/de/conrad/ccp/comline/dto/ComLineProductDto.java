package de.conrad.ccp.comline.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * ComLine Article Data DTO - represents the "artikeldaten" structure from ComLine API response
 */
public record ComLineProductDto(
        @JsonProperty("comline_artikelnummer")
        String comlineArtikelnummer,

        @JsonProperty("comline_artikelbezeichnung")
        String comlineArtikelbezeichnung,

        @JsonProperty("comline_artikelbeschreibung")
        String comlineArtikelbeschreibung,

        @JsonProperty("haendler_ek_netto")
        BigDecimal haendlerEkNetto,

        @JsonProperty("pos_vk_brutto")
        BigDecimal posVkBrutto,

        @JsonProperty("pos_vk")
        BigDecimal posVk,

        @JsonProperty("uvp_netto")
        BigDecimal uvpNetto,

        @JsonProperty("hersteller_artikelnummer")
        String herstellerArtikelnummer,

        @JsonProperty("ean")
        String ean,

        @JsonProperty("artikel_laenge")
        Double artikelLaenge,

        @JsonProperty("artikel_breite")
        Double artikelBreite,

        @JsonProperty("artikel_hoehe")
        Double artikelHoehe,

        @JsonProperty("gewicht_netto")
        Double gewichtNetto,

        @JsonProperty("gewicht_brutto")
        Double gewichtBrutto,

        @JsonProperty("liefertermin")
        OffsetDateTime liefertermin,

        @JsonProperty("url_bild")
        String urlBild,

        @JsonProperty("referenz_artikel")
        String referenzArtikel
) {
}
