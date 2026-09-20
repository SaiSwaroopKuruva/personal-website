package finadvisor.security;

/** Resolves metadata (IP address, user agent) about the HTTP request that triggered the current operation. */
public interface RequestMetadataProvider {
    RequestMetadata current();
}
