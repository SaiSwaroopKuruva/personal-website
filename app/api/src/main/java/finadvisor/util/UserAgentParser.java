package finadvisor.util;

public final class UserAgentParser {

    private UserAgentParser() {
    }

    /** Returns a best-effort human-readable browser name extracted from a User-Agent header. */
    public static String parseBrowser(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "Unknown";
        }
        String ua = userAgent.toLowerCase();
        if (ua.contains("edg/")) {
            return "Microsoft Edge";
        } else if (ua.contains("chrome/") && !ua.contains("chromium")) {
            return "Chrome";
        } else if (ua.contains("firefox/")) {
            return "Firefox";
        } else if (ua.contains("safari/") && !ua.contains("chrome/")) {
            return "Safari";
        }
        return "Other";
    }
}
