package de.conrad.ccp.comline.mapper;

import de.conrad.ccp.comline.api.model.Product;
import de.conrad.ccp.comline.dto.ComLineProductDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.net.URI;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * MapStruct mapper for converting ComLine DTOs to API model objects
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductMapper {

    /**
     * Maps ComLine Product DTO to API Product model
     *
     * Field mappings:
     * - comlineArtikelnummer → productIdentifier
     * - comlineArtikelbezeichnung → name
     * - comlineArtikelbeschreibung → description
     * - haendlerEkNetto → databasePrice (dealer purchase price net)
     * - posVk → retailPrice (POS sales price)
     * - uvpNetto → recommendedRetailPrice (RRP net)
     * - herstellerArtikelnummer → manufacturerProductNumber
     * - artikelLaenge → length
     * - artikelBreite → width
     * - artikelHoehe → height
     * - gewichtNetto → netWeight
     * - gewichtBrutto → productWeight (gross weight)
     * - posVkBrutto → grossPrice (POS sales price gross)
     * - liefertermin → deliveryDate
     * - urlBild → url and productImage
     */
    @Mapping(target = "productIdentifier", source = "comlineArtikelnummer")
    @Mapping(target = "name", source = "comlineArtikelbezeichnung")
    @Mapping(target = "description", source = "comlineArtikelbeschreibung")
    @Mapping(target = "databasePrice", source = "haendlerEkNetto")
    @Mapping(target = "retailPrice", source = "posVk")
    @Mapping(target = "recommendedRetailPrice", source = "uvpNetto")
    @Mapping(target = "manufacturerProductNumber", source = "herstellerArtikelnummer")
    @Mapping(target = "ean", source = "ean")
    @Mapping(target = "length", source = "artikelLaenge")
    @Mapping(target = "width", source = "artikelBreite")
    @Mapping(target = "height", source = "artikelHoehe")
    @Mapping(target = "netWeight", source = "gewichtNetto")
    @Mapping(target = "productWeight", source = "gewichtBrutto")
    @Mapping(target = "grossPrice", source = "posVkBrutto")
    @Mapping(target = "deliveryDate", source = "liefertermin", qualifiedByName = "offsetDateTimeToLocalDate")
    @Mapping(target = "url", source = "urlBild", qualifiedByName = "stringToUri")
    @Mapping(target = "productImage", source = "urlBild", qualifiedByName = "stringToUri")
    Product toProduct(ComLineProductDto dto);

    /**
     * Custom mapping method to convert String to URI
     */
    @Named("stringToUri")
    default URI stringToUri(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        try {
            // Handle protocol-relative URLs (//domain.com/path)
            if (url.startsWith("//")) {
                url = "https:" + url;
            }
            return URI.create(url);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Custom mapping method to convert OffsetDateTime to LocalDate
     */
    @Named("offsetDateTimeToLocalDate")
    default LocalDate offsetDateTimeToLocalDate(OffsetDateTime dateTime) {
        return dateTime != null ? dateTime.toLocalDate() : null;
    }
}
