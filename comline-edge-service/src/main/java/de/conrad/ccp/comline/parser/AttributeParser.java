package de.conrad.ccp.comline.parser;

import de.conrad.ccp.comline.api.model.ProductAttributes;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to parse ComLine product description (comline_artikelbeschreibung)
 * into structured attributes.
 * <p>
 * The description format uses:
 * - Base configuration before the first '#'
 * - '#' prefix for standard attributes
 * - '##' prefix for legal/disclaimer attributes
 * - '-' prefix for additional notes (ignored)
 */
@UtilityClass
public class AttributeParser {

    // Pattern to extract all attributes marked with # or ##
    private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile(
            "(##?)([^#]+)",
            Pattern.DOTALL
    );

    // Pattern to find where an attribute ends (at " -" or newline)
    private static final Pattern ATTRIBUTE_END_PATTERN = Pattern.compile(
            "(.+?)(?:\\s-|\\n)",
            Pattern.DOTALL
    );

    // Categorization patterns with their corresponding builder setters
    private static final List<CategoryRule> CATEGORY_RULES = List.of(
            new CategoryRule(
                    Pattern.compile(".*(M\\d+\\s*(Pro|Max|Ultra)?\\s*Chip|Core\\s*(i\\d+|Ultra)|CPU|Processor|Ryzen|Threadripper).*", Pattern.CASE_INSENSITIVE),
                    ProductAttributes.Builder::processor
            ),
            new CategoryRule(
                    Pattern.compile(".*(\\d+\\s*GB.*(?:Arbeitsspeicher|RAM|Memory|gemeinsam)).*", Pattern.CASE_INSENSITIVE),
                    ProductAttributes.Builder::memory
            ),
            new CategoryRule(
                    Pattern.compile(".*(\\d+\\s*(?:TB|GB)\\s*(?:SSD|NVMe|Speicher|Storage)).*", Pattern.CASE_INSENSITIVE),
                    ProductAttributes.Builder::storage
            ),
            new CategoryRule(
                    Pattern.compile(".*(\\d+W.*(?:Power Adapter|Netzteil|USB-C|Ladegerät)).*", Pattern.CASE_INSENSITIVE),
                    ProductAttributes.Builder::powerAdapter
            ),
            new CategoryRule(
                    Pattern.compile(".*(Keyboard|Tastatur|Magic Keyboard).*", Pattern.CASE_INSENSITIVE),
                    ProductAttributes.Builder::keyboard
            ),
            new CategoryRule(
                    Pattern.compile(".*(Display|Bildschirm|Retina|Glass|Glas|Monitor|Screen).*", Pattern.CASE_INSENSITIVE),
                    ProductAttributes.Builder::display
            )
    );

    /**
     * Parses the ComLine artikelbeschreibung string into structured ProductAttributes
     *
     * @param description the raw description string from comline_artikelbeschreibung
     * @return ProductAttributes object with categorized attributes
     */
    public static ProductAttributes parse(String description) {
        if (description == null || description.isBlank()) {
            return new ProductAttributes();
        }

        ProductAttributes.Builder builder = ProductAttributes.builder();

        extractBaseConfiguration(description, builder);
        extractAttributes(description, builder);

        return builder.build();
    }

    /**
     * Extracts the base configuration (text before first '#')
     */
    private static void extractBaseConfiguration(String description, ProductAttributes.Builder builder) {
        int firstHashIndex = description.indexOf('#');
        if (firstHashIndex <= 0) {
            return;
        }

        String baseConfig = description.substring(0, firstHashIndex).trim();
        if (baseConfig.endsWith(":")) {
            baseConfig = baseConfig.substring(0, baseConfig.length() - 1).trim();
        }

        if (!baseConfig.isEmpty()) {
            builder.baseConfiguration(baseConfig);
        }
    }

    /**
     * Extracts and categorizes all attributes marked with # or ##
     */
    private static void extractAttributes(String description, ProductAttributes.Builder builder) {
        List<String> legalItems = new ArrayList<>();
        List<String> otherItems = new ArrayList<>();

        Matcher matcher = ATTRIBUTE_PATTERN.matcher(description);

        while (matcher.find()) {
            String marker = matcher.group(1);      // "#" or "##"
            String content = matcher.group(2);     // attribute content

            String attribute = cleanAttribute(content);
            if (attribute.isEmpty()) {
                continue;
            }

            if ("##".equals(marker)) {
                legalItems.add(attribute);
            } else {
                categorizeAttribute(builder, otherItems, attribute);
            }
        }

        setListsIfNotEmpty(builder, legalItems, otherItems);
    }

    /**
     * Cleans an attribute by removing trailing notes and whitespace
     */
    private static String cleanAttribute(String content) {
        String trimmed = content.trim();

        // Try to find where the attribute ends (at " -" or newline)
        Matcher endMatcher = ATTRIBUTE_END_PATTERN.matcher(trimmed);
        if (endMatcher.find()) {
            return endMatcher.group(1).trim();
        }

        return trimmed;
    }

    /**
     * Categorizes an attribute by matching against patterns
     */
    private static void categorizeAttribute(ProductAttributes.Builder builder,
                                           List<String> otherItems,
                                           String attribute) {
        for (CategoryRule rule : CATEGORY_RULES) {
            if (rule.pattern().matcher(attribute).matches()) {
                rule.setter().accept(builder, attribute);
                return;
            }
        }

        // No category matched - add to "other"
        otherItems.add(attribute);
    }

    /**
     * Sets legal and other lists only if they contain items
     */
    private static void setListsIfNotEmpty(ProductAttributes.Builder builder,
                                          List<String> legalItems,
                                          List<String> otherItems) {
        if (!legalItems.isEmpty()) {
            builder.legal(legalItems);
        }
        if (!otherItems.isEmpty()) {
            builder.other(otherItems);
        }
    }

    /**
     * Record to hold a pattern and its corresponding setter method
     */
    private record CategoryRule(Pattern pattern, BiConsumer<ProductAttributes.Builder, String> setter) {
    }
}
