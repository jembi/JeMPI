package org.jembi.jempi.api.keycloak;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.common.crypto.CryptoIntegration;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.keycloak.util.SystemPropertiesJsonParserFactory;

public class AkkaKeycloakDeploymentBuilder extends KeycloakDeploymentBuilder {

    protected AkkaKeycloakDeploymentBuilder() {
    }

    public static KeycloakDeployment build(InputStream is) {
        CryptoIntegration.init(org.keycloak.adapters.KeycloakDeploymentBuilder.class.getClassLoader());
        AkkaAdapterConfig adapterConfig = loadAdapterConfig(is);
        return (new AkkaKeycloakDeploymentBuilder()).internalBuild(adapterConfig);
    }

    public static AkkaAdapterConfig loadAdapterConfig(InputStream is) {
        ObjectMapper mapper = new ObjectMapper(new SystemPropertiesJsonParserFactory());
        mapper.setSerializationInclusion(Include.NON_DEFAULT);

        try {
            AkkaAdapterConfig adapterConfig = mapper.readValue(is, AkkaAdapterConfig.class);
            return adapterConfig;
        } catch (IOException var4) {
            throw new RuntimeException(var4);
        }
    }

    public static KeycloakDeployment build(AkkaAdapterConfig adapterConfig) {
        return (new AkkaKeycloakDeploymentBuilder()).internalBuild(adapterConfig);
    }

}
