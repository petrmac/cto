package de.conrad.ccp.comline.parser

import de.conrad.ccp.comline.api.model.ProductAttributes
import spock.lang.Specification

class AttributeParserSpec extends Specification {

    def "should parse full example with all attribute types"() {
        given: "a complete comline_artikelbeschreibung"
        def description = """CTO Variante der Grundkonfiguration MX2Y3D/A: \
#Apple M4 Pro Chip 14‑Core CPU 20‑Core GPU 16-Core NE \
#48 GB gemeinsamer Arbeitsspeicher \
#4TB SSD Speicher \
#140W USB‑C Power Adapter \
#Beleuchtetes Magic Keyboard mit Touch ID (britisch) \
#Standardglas \
##Urheberrechtsabgabe nach §54a UrhG abgeführt und im Kaufpreis enthalten \
- Artikel auftragsbezogen bestellt, Stornierung oder Rückgabe ausgeschlossen \
- We can not accept a cancellation or return for this build to order configuration."""

        when: "parsing the description"
        ProductAttributes result = AttributeParser.parse(description)

        then: "base configuration is extracted"
        result.baseConfiguration == "CTO Variante der Grundkonfiguration MX2Y3D/A"

        and: "processor is categorized"
        result.processor == "Apple M4 Pro Chip 14‑Core CPU 20‑Core GPU 16-Core NE"

        and: "memory is categorized"
        result.memory == "48 GB gemeinsamer Arbeitsspeicher"

        and: "storage is categorized"
        result.storage == "4TB SSD Speicher"

        and: "power adapter is categorized"
        result.powerAdapter == "140W USB‑C Power Adapter"

        and: "keyboard is categorized"
        result.keyboard == "Beleuchtetes Magic Keyboard mit Touch ID (britisch)"

        and: "display is categorized"
        result.display == "Standardglas"

        and: "legal notices are extracted"
        result.legal.size() == 1
        result.legal[0] == "Urheberrechtsabgabe nach §54a UrhG abgeführt und im Kaufpreis enthalten"
    }

    def "should handle null or empty description"() {
        when: "parsing null description"
        ProductAttributes result1 = AttributeParser.parse(null)

        then: "returns empty attributes"
        result1 != null
        result1.baseConfiguration == null
        result1.processor == null

        when: "parsing empty description"
        ProductAttributes result2 = AttributeParser.parse("")

        then: "returns empty attributes"
        result2 != null
        result2.baseConfiguration == null
        result2.processor == null
    }

    def "should parse description without base configuration"() {
        given: "a description starting with attributes"
        def description = "#Apple M4 Pro Chip #64 GB RAM #2TB SSD Speicher"

        when: "parsing the description"
        ProductAttributes result = AttributeParser.parse(description)

        then: "base configuration is null"
        result.baseConfiguration == null

        and: "attributes are parsed"
        result.processor == "Apple M4 Pro Chip"
        result.memory == "64 GB RAM"
        result.storage == "2TB SSD Speicher"
    }

    def "should categorize various memory formats"() {
        expect: "different memory formats are recognized"
        AttributeParser.parse("#$input").memory == input

        where:
        input << [
                "48 GB gemeinsamer Arbeitsspeicher",
                "64GB RAM",
                "128 GB Memory",
                "16GB Arbeitsspeicher"
        ]
    }

    def "should categorize various storage formats"() {
        expect: "different storage formats are recognized"
        AttributeParser.parse("#$input").storage == input

        where:
        input << [
                "4TB SSD Speicher",
                "2TB NVMe",
                "512GB SSD",
                "1 TB SSD Storage"
        ]
    }

    def "should categorize various processor formats"() {
        expect: "different processor formats are recognized"
        AttributeParser.parse("#$input").processor == input

        where:
        input << [
                "Apple M4 Pro Chip 14‑Core CPU",
                "M4 Max Chip",
                "Intel Core i9 Processor",
                "AMD Ryzen 9 CPU"
        ]
    }

    def "should put uncategorized attributes in other list"() {
        given: "a description with uncategorized attributes"
        def description = "#Some random feature #Another custom option"

        when: "parsing the description"
        ProductAttributes result = AttributeParser.parse(description)

        then: "uncategorized items are in other list"
        result.other.size() == 2
        result.other.contains("Some random feature")
        result.other.contains("Another custom option")
    }

    def "should handle multiple legal notices"() {
        given: "a description with multiple legal items"
        def description = """#Apple M4 Pro \
##Urheberrechtsabgabe nach §54a UrhG \
##Warranty information applies \
##Additional legal disclaimer"""

        when: "parsing the description"
        ProductAttributes result = AttributeParser.parse(description)

        then: "all legal items are extracted"
        result.legal.size() == 3
        result.legal.contains("Urheberrechtsabgabe nach §54a UrhG")
        result.legal.contains("Warranty information applies")
        result.legal.contains("Additional legal disclaimer")
    }

    def "should stop parsing at dash separator"() {
        given: "a description with dash-separated notes"
        def description = "#Apple M4 Pro Chip - This is a note that should be ignored #64 GB RAM - another note"

        when: "parsing the description"
        ProductAttributes result = AttributeParser.parse(description)

        then: "attributes are parsed without the notes"
        result.processor == "Apple M4 Pro Chip"
        result.memory == "64 GB RAM"
    }

    def "should handle keyboard variations"() {
        expect: "different keyboard formats are recognized"
        AttributeParser.parse("#$input").keyboard == input

        where:
        input << [
                "Beleuchtetes Magic Keyboard mit Touch ID (britisch)",
                "Magic Keyboard (US)",
                "Wireless Keyboard",
                "Tastatur QWERTZ"
        ]
    }

    def "should handle display variations"() {
        expect: "different display formats are recognized"
        AttributeParser.parse("#$input").display == input

        where:
        input << [
                "Standardglas",
                "Retina Display",
                "Nano-texture glass",
                "14-inch Display"
        ]
    }

    def "should handle power adapter variations"() {
        expect: "different power adapter formats are recognized"
        AttributeParser.parse("#$input").powerAdapter == input

        where:
        input << [
                "140W USB‑C Power Adapter",
                "96W Netzteil",
                "67W USB-C Ladegerät",
                "30W Power Adapter"
        ]
    }
}
