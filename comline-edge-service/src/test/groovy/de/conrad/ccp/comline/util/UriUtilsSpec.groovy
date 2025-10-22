package de.conrad.ccp.comline.util

import spock.lang.Specification
import spock.lang.Unroll

class UriUtilsSpec extends Specification {

    @Unroll
    def "stringToUri should handle '#description'"() {
        when: "converting URL to URI"
        def result = UriUtils.stringToUri(input)

        then: "result matches expected outcome"
        result.isPresent() == expectedPresent
        if (expectedPresent) {
            assert result.get().toString() == expectedUri
            if (expectedScheme != null) {
                assert result.get().scheme == expectedScheme
            }
            if (expectedHost != null) {
                assert result.get().host == expectedHost
            }
            if (expectedPath != null) {
                assert result.get().path == expectedPath
            }
        }

        where:
        description                              | input                                                                      | expectedPresent | expectedUri                                                                   | expectedScheme | expectedHost               | expectedPath
        // Valid URLs - Protocol-relative
        "protocol-relative URL with path"        | "//example.com/image.jpg"                                                  | true            | "https://example.com/image.jpg"                                               | "https"        | "example.com"              | "/image.jpg"
        "protocol-relative Cloudinary URL"       | "//ezentrum-res.cloudinary.com/image/upload/v1730460185/comline/test.jpg" | true            | "https://ezentrum-res.cloudinary.com/image/upload/v1730460185/comline/test.jpg" | "https"        | "ezentrum-res.cloudinary.com" | "/image/upload/v1730460185/comline/test.jpg"
        "protocol-relative URL with query"       | "//api.example.com/data?key=value"                                         | true            | "https://api.example.com/data?key=value"                                      | "https"        | "api.example.com"          | "/data"
        "protocol-relative URL with port"        | "//example.com:8080/path"                                                  | true            | "https://example.com:8080/path"                                               | "https"        | "example.com"              | "/path"
        "protocol-relative URL subdomain"        | "//cdn.example.com/assets/style.css"                                       | true            | "https://cdn.example.com/assets/style.css"                                    | "https"        | "cdn.example.com"          | "/assets/style.css"

        // Valid URLs - Absolute HTTPS
        "absolute HTTPS URL"                     | "https://example.com/image.jpg"                                            | true            | "https://example.com/image.jpg"                                               | "https"        | "example.com"              | "/image.jpg"
        "absolute HTTPS with subdomain"          | "https://cdn.example.com/img.png"                                          | true            | "https://cdn.example.com/img.png"                                             | "https"        | "cdn.example.com"          | "/img.png"
        "absolute HTTPS with query params"       | "https://example.com/api?id=123&type=json"                                 | true            | "https://example.com/api?id=123&type=json"                                    | "https"        | "example.com"              | "/api"
        "absolute HTTPS with fragment"           | "https://example.com/page#section"                                         | true            | "https://example.com/page#section"                                            | "https"        | "example.com"              | "/page"
        "absolute HTTPS with port"               | "https://example.com:443/secure"                                           | true            | "https://example.com:443/secure"                                              | "https"        | "example.com"              | "/secure"
        "absolute HTTPS with auth"               | "https://user:pass@example.com/path"                                       | true            | "https://user:pass@example.com/path"                                          | "https"        | "example.com"              | "/path"

        // Valid URLs - Absolute HTTP
        "absolute HTTP URL"                      | "http://example.com/page.html"                                             | true            | "http://example.com/page.html"                                                | "http"         | "example.com"              | "/page.html"
        "absolute HTTP with port 8080"           | "http://localhost:8080/api"                                                | true            | "http://localhost:8080/api"                                                   | "http"         | "localhost"                | "/api"
        "absolute HTTP with IP address"          | "http://192.168.1.1/admin"                                                 | true            | "http://192.168.1.1/admin"                                                    | "http"         | "192.168.1.1"              | "/admin"

        // Valid URLs - Other protocols
        "FTP URL"                                | "ftp://files.example.com/download.zip"                                     | true            | "ftp://files.example.com/download.zip"                                        | "ftp"          | "files.example.com"        | "/download.zip"
        "file protocol URL"                      | "file:///home/user/document.pdf"                                           | true            | "file:///home/user/document.pdf"                                              | "file"         | null                       | "/home/user/document.pdf"

        // Edge cases - Valid
        "URL with special characters in path"    | "https://example.com/path%20with%20spaces"                                 | true            | "https://example.com/path%20with%20spaces"                                    | "https"        | "example.com"              | "/path with spaces"
        "URL with unicode domain (IDN)"          | "https://münchen.de/index.html"                                            | true            | "https://münchen.de/index.html"                                               | "https"        | null                       | "/index.html"
        "URL with multiple query params"         | "https://example.com/search?q=test&page=1&sort=asc"                        | true            | "https://example.com/search?q=test&page=1&sort=asc"                           | "https"        | "example.com"              | "/search"
        "URL with deep path"                     | "https://example.com/a/b/c/d/e/f/file.txt"                                 | true            | "https://example.com/a/b/c/d/e/f/file.txt"                                    | "https"        | "example.com"              | "/a/b/c/d/e/f/file.txt"

        // Invalid/Empty inputs
        "null input"                             | null                                                                       | false           | null                                                                          | null           | null                       | null
        "empty string"                           | ""                                                                         | false           | null                                                                          | null           | null                       | null
        "blank string with spaces"               | "   "                                                                      | false           | null                                                                          | null           | null                       | null
        "blank string with tab"                  | "\t"                                                                       | false           | null                                                                          | null           | null                       | null
        "blank string with newline"              | "\n"                                                                       | false           | null                                                                          | null           | null                       | null

        // Invalid URL formats
        "invalid URL with spaces"                | "not a valid url"                                                          | false           | null                                                                          | null           | null                       | null
        "invalid URL with only protocol"         | "https://"                                                                 | false           | null                                                                          | null           | null                       | null
        "invalid URL malformed"                  | "ht!tp://example.com"                                                      | false           | null                                                                          | null           | null                       | null
        "invalid URL with brackets"              | "https://[example].com"                                                    | false           | null                                                                          | null           | null                       | null
    }

    def "stringToUri should return empty Optional for null"() {
        when: "converting null URL"
        def result = UriUtils.stringToUri(null)

        then: "result is empty Optional"
        !result.isPresent()
        result == Optional.empty()
    }

    def "stringToUri should return empty Optional for blank strings"() {
        expect: "empty Optional for all blank inputs"
        !UriUtils.stringToUri(input).isPresent()

        where:
        input << ["", "   ", "\t", "\n", "\r\n", "  \t  \n  "]
    }

    def "stringToUri should handle real ComLine image URLs"() {
        given: "real ComLine image URL"
        def url = "//ezentrum-res.cloudinary.com/image/upload/v1730460185/comline/nj7tg26s7sjjny01rcfj.jpg"

        when: "converting to URI"
        def result = UriUtils.stringToUri(url)

        then: "result is valid HTTPS URI"
        result.isPresent()
        result.get().scheme == "https"
        result.get().host == "ezentrum-res.cloudinary.com"
        result.get().path.startsWith("/image/upload/")
        result.get().toString().startsWith("https://")
    }

    def "stringToUri should preserve query parameters"() {
        given: "URL with query parameters"
        def url = "https://api.example.com/products?category=electronics&sort=price&order=asc"

        when: "converting to URI"
        def result = UriUtils.stringToUri(url)

        then: "query parameters are preserved"
        result.isPresent()
        result.get().query == "category=electronics&sort=price&order=asc"
        result.get().toString() == url
    }

    def "stringToUri should preserve URL fragments"() {
        given: "URL with fragment"
        def url = "https://example.com/docs#section-2.3"

        when: "converting to URI"
        def result = UriUtils.stringToUri(url)

        then: "fragment is preserved"
        result.isPresent()
        result.get().fragment == "section-2.3"
        result.get().toString() == url
    }

    def "stringToUri should handle IPv6 addresses"() {
        given: "URL with IPv6 address"
        def url = "http://[2001:db8::1]/path"

        when: "converting to URI"
        def result = UriUtils.stringToUri(url)

        then: "IPv6 address is handled correctly"
        result.isPresent()
        result.get().scheme == "http"
        result.get().host == "[2001:db8::1]" // Java URI includes brackets for IPv6
    }

    def "stringToUri should be consistent across multiple calls"() {
        given: "a valid URL"
        def url = "//cdn.example.com/image.jpg"

        when: "converting multiple times"
        def result1 = UriUtils.stringToUri(url)
        def result2 = UriUtils.stringToUri(url)
        def result3 = UriUtils.stringToUri(url)

        then: "all results are identical"
        result1.isPresent()
        result2.isPresent()
        result3.isPresent()
        result1.get() == result2.get()
        result2.get() == result3.get()
        result1.get().toString() == "https://cdn.example.com/image.jpg"
    }
}
