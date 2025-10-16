package de.conrad.ccp.comline.mapper

import de.conrad.ccp.comline.dto.ComLineProductDto
import org.mapstruct.factory.Mappers
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset

class ProductMapperSpec extends Specification {

    @Subject
    ProductMapper mapper = Mappers.getMapper(ProductMapper.class)

    def "should map ComLineProductDto to Product with all fields"() {
        given: "a complete ComLine Product DTO"
        def dto = new ComLineProductDto(
                "CZ1FU-013020",                                    // comlineArtikelnummer
                "Apple MacBook Pro 16'' SpaceSchwarz CTO M4 Pro", // comlineArtikelbezeichnung
                "CTO Variante der Grundkonfiguration MX2Y3D/A",   // comlineArtikelbeschreibung
                new BigDecimal("3610.97"),                         // haendlerEkNetto
                new BigDecimal("4899"),                            // posVkBrutto
                new BigDecimal("4899"),                            // posVk
                new BigDecimal("4116.81"),                         // uvpNetto
                "Z1FU-013020",                                     // herstellerArtikelnummer
                "4069116466182",                                   // ean
                0.4,                                               // artikelLaenge
                0.075,                                             // artikelBreite
                0.29,                                              // artikelHoehe
                2.1,                                               // gewichtNetto
                3.5,                                               // gewichtBrutto
                OffsetDateTime.of(2025, 11, 6, 0, 0, 0, 0, ZoneOffset.UTC), // liefertermin
                "//ezentrum-res.cloudinary.com/image/upload/v1730460185/comline/nj7tg26s7sjjny01rcfj.jpg", // urlBild
                ""                                                 // referenzArtikel
        )

        when: "mapping to Product"
        def product = mapper.toProduct(dto)

        then: "all fields are mapped correctly"
        product.productIdentifier == "CZ1FU-013020"
        product.name == "Apple MacBook Pro 16'' SpaceSchwarz CTO M4 Pro"
        product.description == "CTO Variante der Grundkonfiguration MX2Y3D/A"
        product.databasePrice == new BigDecimal("3610.97")
        product.retailPrice == new BigDecimal("4899")
        product.recommendedRetailPrice == new BigDecimal("4116.81")
        product.manufacturerProductNumber == "Z1FU-013020"
        product.ean == "4069116466182"
        product.length == 0.4
        product.width == 0.075
        product.height == 0.29
        product.netWeight == 2.1
        product.productWeight == 3.5
        product.grossPrice == new BigDecimal("4899")
        product.deliveryDate == LocalDate.of(2025, 11, 6)
        product.url.toString() == "https://ezentrum-res.cloudinary.com/image/upload/v1730460185/comline/nj7tg26s7sjjny01rcfj.jpg"
        product.productImage.toString() == "https://ezentrum-res.cloudinary.com/image/upload/v1730460185/comline/nj7tg26s7sjjny01rcfj.jpg"
    }

    def "should handle null values gracefully"() {
        given: "a DTO with minimal fields"
        def dto = new ComLineProductDto(
                "CZ1FU-013020",
                "Test Product",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        )

        when: "mapping to Product"
        def product = mapper.toProduct(dto)

        then: "non-null fields are mapped, nulls are preserved"
        product.productIdentifier == "CZ1FU-013020"
        product.name == "Test Product"
        product.description == null
        product.databasePrice == null
        product.deliveryDate == null
        product.url == null
        product.productImage == null
    }

    def "should convert protocol-relative URL to HTTPS"() {
        given: "a protocol-relative URL"
        def url = "//example.com/image.jpg"

        when: "converting to URI"
        def uri = mapper.stringToUri(url)

        then: "HTTPS protocol is added"
        uri.toString() == "https://example.com/image.jpg"
        uri.scheme == "https"
        uri.host == "example.com"
        uri.path == "/image.jpg"
    }

    def "should handle absolute HTTPS URL"() {
        given: "an absolute HTTPS URL"
        def url = "https://example.com/image.jpg"

        when: "converting to URI"
        def uri = mapper.stringToUri(url)

        then: "URL is preserved"
        uri.toString() == "https://example.com/image.jpg"
        uri.scheme == "https"
    }

    def "should handle absolute HTTP URL"() {
        given: "an absolute HTTP URL"
        def url = "http://example.com/image.jpg"

        when: "converting to URI"
        def uri = mapper.stringToUri(url)

        then: "URL is preserved"
        uri.toString() == "http://example.com/image.jpg"
        uri.scheme == "http"
    }

    def "should return null for null or blank URLs"() {
        expect: "null for null/blank input"
        mapper.stringToUri(url) == null

        where:
        url << [null, "", "   ", "\t"]
    }

    def "should return null for invalid URLs"() {
        given: "an invalid URL"
        def url = "not a valid url"

        when: "converting to URI"
        def uri = mapper.stringToUri(url)

        then: "returns null"
        uri == null
    }

    def "should convert OffsetDateTime to LocalDate"() {
        given: "an OffsetDateTime"
        def dateTime = OffsetDateTime.of(2025, 11, 6, 14, 30, 0, 0, ZoneOffset.UTC)

        when: "converting to LocalDate"
        def localDate = mapper.offsetDateTimeToLocalDate(dateTime)

        then: "only date part is kept"
        localDate == LocalDate.of(2025, 11, 6)
    }

    def "should handle null OffsetDateTime"() {
        expect: "null for null input"
        mapper.offsetDateTimeToLocalDate(null) == null
    }

    def "should preserve time zone when converting to LocalDate"() {
        given: "OffsetDateTime with different time zones"
        def utcDateTime = OffsetDateTime.of(2025, 11, 6, 23, 0, 0, 0, ZoneOffset.UTC)
        def estDateTime = OffsetDateTime.of(2025, 11, 6, 18, 0, 0, 0, ZoneOffset.ofHours(-5))

        when: "converting to LocalDate"
        def utcDate = mapper.offsetDateTimeToLocalDate(utcDateTime)
        def estDate = mapper.offsetDateTimeToLocalDate(estDateTime)

        then: "local date is based on the time zone"
        utcDate == LocalDate.of(2025, 11, 6)
        estDate == LocalDate.of(2025, 11, 6)
    }

    def "should map real ComLine API response data"() {
        given: "actual data from ComLine API"
        def dto = new ComLineProductDto(
                "CZ1FU-013020",                                    // comlineArtikelnummer
                "Apple MacBook Pro 16'' SpaceSchwarz CTO M4 Pro 14-Core CPU 20-Core GPU (48GB,4TB,britisch)", // comlineArtikelbezeichnung
                "CTO Variante der Grundkonfiguration MX2Y3D/A: #Apple M4 Pro Chip 14‑Core CPU 20‑Core GPU 16-Core NE #48 GB gemeinsamer Arbeitsspeicher #4TB SSD Speicher #140W USB‑C Power Adapter #Beleuchtetes Magic Keyboard mit Touch ID (britisch) #Standardglas ##Urheberrechtsabgabe nach §54a UrhG abgeführt und im Kaufpreis enthalten - Artikel auftragsbezogen bestellt, Stornierung oder Rückgabe ausgeschlossen - We can not accept a cancellation or return for this build to order configuration.", // comlineArtikelbeschreibung
                new BigDecimal("3610.97"),                         // haendlerEkNetto
                new BigDecimal("4899"),                            // posVkBrutto
                new BigDecimal("4899"),                            // posVk
                new BigDecimal("4116.81"),                         // uvpNetto
                "Z1FU-013020",                                     // herstellerArtikelnummer
                "4069116466182",                                   // ean
                0.4,                                               // artikelLaenge
                0.075,                                             // artikelBreite
                0.29,                                              // artikelHoehe
                2.1,                                               // gewichtNetto
                3.5,                                               // gewichtBrutto
                OffsetDateTime.parse("2025-11-06T00:00:00Z"),     // liefertermin
                "//ezentrum-res.cloudinary.com/image/upload/v1730460185/comline/nj7tg26s7sjjny01rcfj.jpg", // urlBild
                ""                                                 // referenzArtikel
        )

        when: "mapping to Product"
        def product = mapper.toProduct(dto)

        then: "product matches expected values"
        product.productIdentifier == "CZ1FU-013020"
        product.name.contains("Apple MacBook Pro")
        product.databasePrice.compareTo(new BigDecimal("3610.97")) == 0
        product.ean == "4069116466182"
        product.deliveryDate == LocalDate.of(2025, 11, 6)
        product.url.toString().startsWith("https://")
        product.url.toString().contains("cloudinary")
    }
}
