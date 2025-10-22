package de.conrad.ccp.comline.parser;

import de.conrad.ccp.comline.api.model.ProductAttributes;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
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

    // Regex patterns for categorization
    private static final Pattern PROCESSOR_PATTERN = Pattern.compile(
            ".*(M\\d+\\s*(Pro|Max|Ultra)?\\s*Chip|Core\\s*(i\\d+|Ultra)|CPU|Processor|Ryzen|Threadripper).*",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern MEMORY_PATTERN = Pattern.compile(
            ".*(\\d+\\s*GB.*(?:Arbeitsspeicher|RAM|Memory|gemeinsam)).*",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern STORAGE_PATTERN = Pattern.compile(
            ".*(\\d+\\s*(?:TB|GB)\\s*(?:SSD|NVMe|Speicher|Storage)).*",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern POWER_ADAPTER_PATTERN = Pattern.compile(
            ".*(\\d+W.*(?:Power Adapter|Netzteil|USB-C|Ladegerät)).*",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern KEYBOARD_PATTERN = Pattern.compile(
            ".*(Keyboard|Tastatur|Magic Keyboard).*",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern DISPLAY_PATTERN = Pattern.compile(
            ".*(Display|Bildschirm|Retina|Glass|Glas|Monitor|Screen).*",
            Pattern.CASE_INSENSITIVE
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
        List<String> legalItems = new ArrayList<>();
        List<String> otherItems = new ArrayList<>();

        // Extract base configuration (text before first '#')
        int firstHashIndex = description.indexOf('#');
        if (firstHashIndex > 0) {
            String baseConfig = description.substring(0, firstHashIndex).trim();
            // Remove trailing colon if present
            if (baseConfig.endsWith(":")) {
                baseConfig = baseConfig.substring(0, baseConfig.length() - 1).trim();
            }
            builder.baseConfiguration(baseConfig);
        }

        // Split by single '#' but preserve '##' markers
        // We'll manually parse to handle ## correctly
        int currentIndex = firstHashIndex;
        while (currentIndex < description.length() && currentIndex >= 0) {
            currentIndex = description.indexOf('#', currentIndex);
            if (currentIndex < 0) {
                break;
            }

            boolean isLegal = false;
            int contentStart = currentIndex + 1;

            // Check if this is a legal notice (##)
            if (contentStart < description.length() && description.charAt(contentStart) == '#') {
                isLegal = true;
                contentStart++;
            }

            // Find the next '#' or end of string
            int nextHash = description.indexOf('#', contentStart);
            String part;
            if (nextHash > 0) {
                part = description.substring(contentStart, nextHash).trim();
                currentIndex = nextHash;
            } else {
                part = description.substring(contentStart).trim();
                currentIndex = -1; // No more attributes
            }

            // Stop at section markers (dashes, bullets, etc.)
            int endIndex = findAttributeEnd(part);
            if (endIndex > 0) {
                part = part.substring(0, endIndex).trim();
            }

            if (part.isEmpty()) {
                continue;
            }

            if (isLegal) {
                legalItems.add(part);
            } else {
                categorizeAttribute(builder, otherItems, part);
            }
        }

        // Set legal and other items
        if (!legalItems.isEmpty()) {
            builder.legal(legalItems);
        }
        if (!otherItems.isEmpty()) {
            builder.other(otherItems);
        }

        return builder.build();
    }

    /**
     * Finds the end of an attribute value (stops at '-', newlines, etc.)
     */
    private static int findAttributeEnd(String part) {
        // Look for common separators that indicate end of attribute
        int dashIndex = part.indexOf(" -");
        int newlineIndex = part.indexOf('\n');

        if (dashIndex > 0 && (newlineIndex < 0 || dashIndex < newlineIndex)) {
            return dashIndex;
        }
        if (newlineIndex > 0) {
            return newlineIndex;
        }
        return -1;
    }

    /**
     * Categorizes an attribute based on regex patterns
     */
    private static void categorizeAttribute(ProductAttributes.Builder builder, List<String> otherItems, String attribute) {
        if (PROCESSOR_PATTERN.matcher(attribute).matches()) {
            builder.processor(attribute);
        } else if (MEMORY_PATTERN.matcher(attribute).matches()) {
            builder.memory(attribute);
        } else if (STORAGE_PATTERN.matcher(attribute).matches()) {
            builder.storage(attribute);
        } else if (POWER_ADAPTER_PATTERN.matcher(attribute).matches()) {
            builder.powerAdapter(attribute);
        } else if (KEYBOARD_PATTERN.matcher(attribute).matches()) {
            builder.keyboard(attribute);
        } else if (DISPLAY_PATTERN.matcher(attribute).matches()) {
            builder.display(attribute);
        } else {
            // Uncategorized attribute
            otherItems.add(attribute);
        }
    }
}
