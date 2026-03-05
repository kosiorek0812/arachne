package dev.kosiorek0812.arachne;

public class Url {

    // Constructor — accepts a raw string URL
    public Url(String rawUrl) {

    }

    // Returns the normalized string representation of this URL
    public String getValue() {
        return null;
    }

    // Returns true if this URL belongs to the same domain as the given URL
    // Used by the Crawler to restrict crawling to a single domain
    public boolean isSameDomain(Url other) {
        return false;
    }

    // Returns true if the URL is likely crawlable (http or https scheme)
    // Filters out mailto:, javascript:, ftp: etc.
    public boolean isCrawlable() {
        return false;
    }

    // Resolves a relative link against this URL and returns a new Url
    // e.g. this = "https://example.com/page", link = "/about" → "https://example.com/about"
    public Url resolve(String relativeLink) {
        return null;
    }

    // Standard equals and hashCode — needed for the visited set in Frontier
    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String toString() {
        return null;
    }
}
