package dev.kosiorek0812.arachne;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Url")
class UrlTest {

    // -------------------------------------------------------------------------
    // Construction & getValue
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("getValue()")
    class GetValue {

        @Test
        @DisplayName("returns the raw URL string when already normalized")
        void returnsRawUrlForNormalInput() {
            Url url = new Url("https://example.com/page");
            assertEquals("https://example.com/page", url.getValue());
        }

        @Test
        @DisplayName("strips trailing slash from path so /page/ and /page are the same")
        void normalizesTrailingSlash() {
            Url withSlash    = new Url("https://example.com/page/");
            Url withoutSlash = new Url("https://example.com/page");
            assertEquals(withoutSlash.getValue(), withSlash.getValue());
        }

        @Test
        @DisplayName("lowercases scheme and host")
        void lowercasesSchemeAndHost() {
            Url url = new Url("HTTPS://Example.COM/Path");
            assertTrue(url.getValue().startsWith("https://example.com"));
        }

        @Test
        @DisplayName("preserves query string")
        void preservesQueryString() {
            Url url = new Url("https://example.com/search?q=java");
            assertTrue(url.getValue().contains("q=java"));
        }

        @Test
        @DisplayName("strips URL fragment because fragments are client-side only")
        void stripsFragment() {
            Url url = new Url("https://example.com/page#section");
            assertFalse(url.getValue().contains("#section"));
        }
    }

    // -------------------------------------------------------------------------
    // isSameDomain
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("isSameDomain()")
    class IsSameDomain {

        @Test
        @DisplayName("returns true for two URLs on the exact same host")
        void trueForSameHost() {
            Url a = new Url("https://example.com/page");
            Url b = new Url("https://example.com/other");
            assertTrue(a.isSameDomain(b));
        }

        @Test
        @DisplayName("returns false for different hosts")
        void falseForDifferentHost() {
            Url a = new Url("https://example.com/page");
            Url b = new Url("https://other.com/page");
            assertFalse(a.isSameDomain(b));
        }

        @Test
        @DisplayName("treats subdomain as a different domain from the apex")
        void subdomainIsDifferentFromApex() {
            Url apex      = new Url("https://example.com/");
            Url subdomain = new Url("https://blog.example.com/");
            assertFalse(apex.isSameDomain(subdomain));
        }

        @Test
        @DisplayName("is symmetric — a.isSameDomain(b) == b.isSameDomain(a)")
        void isSymmetric() {
            Url a = new Url("https://example.com/foo");
            Url b = new Url("https://example.com/bar");
            assertEquals(a.isSameDomain(b), b.isSameDomain(a));
        }

        @Test
        @DisplayName("host comparison is case-insensitive")
        void hostComparisonIsCaseInsensitive() {
            Url lower = new Url("https://example.com/page");
            Url upper = new Url("https://EXAMPLE.COM/page");
            assertTrue(lower.isSameDomain(upper));
        }

        @Test
        @DisplayName("ignores port when comparing domains")
        void ignoresPort() {
            Url withPort    = new Url("https://example.com:8080/page");
            Url withoutPort = new Url("https://example.com/page");
            assertTrue(withPort.isSameDomain(withoutPort));
        }
    }

    // -------------------------------------------------------------------------
    // isCrawlable
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("isCrawlable()")
    class IsCrawlable {

        @ParameterizedTest(name = "{0} is crawlable")
        @ValueSource(strings = {
                "https://example.com/page",
                "http://example.com/page",
                "http://example.com/"
        })
        @DisplayName("returns true for http and https URLs")
        void trueForHttpAndHttps(String raw) {
            assertTrue(new Url(raw).isCrawlable());
        }

        @ParameterizedTest(name = "{0} is NOT crawlable")
        @ValueSource(strings = {
                "mailto:user@example.com",
                "javascript:void(0)",
                "ftp://files.example.com/file.zip",
                "tel:+48123456789",
                "#anchor-only"
        })
        @DisplayName("returns false for non-HTTP schemes")
        void falseForNonHttpSchemes(String raw) {
            assertFalse(new Url(raw).isCrawlable());
        }
    }

    // -------------------------------------------------------------------------
    // resolve
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("resolve()")
    class Resolve {

        @Test
        @DisplayName("resolves an absolute-path relative link")
        void resolvesAbsolutePath() {
            Url base     = new Url("https://example.com/blog/post");
            Url resolved = base.resolve("/about");
            assertEquals("https://example.com/about", resolved.getValue());
        }

        @Test
        @DisplayName("resolves a relative-path link against the current directory")
        void resolvesRelativePath() {
            Url base     = new Url("https://example.com/blog/post");
            Url resolved = base.resolve("next-post");
            assertEquals("https://example.com/blog/next-post", resolved.getValue());
        }

        @Test
        @DisplayName("returns the absolute URL unchanged when the link is already absolute")
        void keepsAbsoluteLink() {
            Url base     = new Url("https://example.com/page");
            Url resolved = base.resolve("https://other.com/resource");
            assertEquals("https://other.com/resource", resolved.getValue());
        }

        @Test
        @DisplayName("strips fragment from the resolved URL")
        void stripsFragmentAfterResolve() {
            Url base     = new Url("https://example.com/page");
            Url resolved = base.resolve("/section#heading");
            assertFalse(resolved.getValue().contains("#heading"));
        }

        @Test
        @DisplayName("resolved URL is itself a valid Url — isCrawlable() works on it")
        void resolvedUrlIsFullyFunctional() {
            Url base     = new Url("https://example.com/");
            Url resolved = base.resolve("/contact");
            assertTrue(resolved.isCrawlable());
        }
    }

    // -------------------------------------------------------------------------
    // equals & hashCode — critical for Frontier's HashSet
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("equals() and hashCode()")
    class EqualsAndHashCode {

        @Test
        @DisplayName("two Urls built from the same string are equal")
        void equalForSameString() {
            Url a = new Url("https://example.com/page");
            Url b = new Url("https://example.com/page");
            assertEquals(a, b);
        }

        @Test
        @DisplayName("equal URLs have the same hashCode — HashSet contract")
        void sameHashCodeWhenEqual() {
            Url a = new Url("https://example.com/page");
            Url b = new Url("https://example.com/page");
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("URLs that normalize to the same string are equal (e.g. trailing slash)")
        void normalizedUrlsAreEqual() {
            Url withSlash    = new Url("https://example.com/page/");
            Url withoutSlash = new Url("https://example.com/page");
            assertEquals(withSlash, withoutSlash);
        }

        @Test
        @DisplayName("different paths are not equal")
        void notEqualForDifferentPaths() {
            Url a = new Url("https://example.com/page");
            Url b = new Url("https://example.com/other");
            assertNotEquals(a, b);
        }

        @Test
        @DisplayName("not equal to null")
        void notEqualToNull() {
            Url url = new Url("https://example.com/page");
            assertNotEquals(null, url);
        }

        @Test
        @DisplayName("not equal to an object of a different type")
        void notEqualToDifferentType() {
            Url url = new Url("https://example.com/page");
            assertNotEquals("https://example.com/page", url);
        }

        @Test
        @DisplayName("equals is reflexive — a URL equals itself")
        void isReflexive() {
            Url url = new Url("https://example.com/page");
            assertEquals(url, url);
        }

        @Test
        @DisplayName("equals is symmetric — a.equals(b) == b.equals(a)")
        void isSymmetric() {
            Url a = new Url("https://example.com/page");
            Url b = new Url("https://example.com/page");
            assertEquals(a.equals(b), b.equals(a));
        }
    }

    // -------------------------------------------------------------------------
    // toString
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("toString()")
    class ToStringTests {

        @Test
        @DisplayName("returns the same value as getValue()")
        void matchesGetValue() {
            Url url = new Url("https://example.com/page");
            assertEquals(url.getValue(), url.toString());
        }

        @Test
        @DisplayName("is non-null")
        void isNotNull() {
            assertNotNull(new Url("https://example.com").toString());
        }
    }
}
