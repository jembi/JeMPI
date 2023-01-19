package org.jembi.jempi.api.keycloak;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.keycloak.common.util.EnvUtil;
import org.keycloak.representations.adapters.config.AdapterConfig;

import java.util.Map;
import java.util.TreeMap;


@JsonPropertyOrder({"realm", "realm-public-key", "auth-server-url", "redirect-uri", "ssl-required", "resource", "public-client", "credentials", "use-resource-role-mappings", "enable-cors", "cors-max-age", "cors-allowed-methods", "cors-exposed-headers", "expose-token", "bearer-only", "autodetect-bearer-only", "connection-pool-size", "socket-timeout-millis", "connection-ttl-millis", "connection-timeout-millis", "allow-any-hostname", "disable-trust-manager", "truststore", "truststore-password", "client-keystore", "client-keystore-password", "client-key-password", "always-refresh-token", "register-node-at-startup", "register-node-period", "token-store", "adapter-state-cookie-path", "principal-attribute", "proxy-url", "turn-off-change-session-id-on-login", "token-minimum-time-to-live", "min-time-between-jwks-requests", "public-key-cache-ttl", "policy-enforcer", "ignore-oauth-query-parameter", "verify-token-audience"})
public class AkkaAdapterConfig extends AdapterConfig {
    @JsonProperty("redirect-uri")
    protected String redirectUri;

    public String getRealm() {
        return EnvUtil.replace(this.realm);
    }

    public String getResource() {
        return EnvUtil.replace(this.resource);
    }

    public String getAuthServerUrl() {
        return EnvUtil.replace(this.authServerUrl);
    }

    public Map<String, Object> getCredentials() {
        Map<String, Object> credentials = new TreeMap(String.CASE_INSENSITIVE_ORDER);
        for (Map.Entry<String, Object> entry : this.credentials.entrySet()) {
            if (entry.getValue() instanceof String) {
                credentials.put(entry.getKey(),  EnvUtil.replace((String) entry.getValue()));
            } else {
                credentials.put(entry.getKey(), entry.getValue());
            }
        }
        return credentials;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getRedirectUri() {
        return EnvUtil.replace(this.redirectUri);
    }
}
