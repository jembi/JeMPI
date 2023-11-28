package org.jembi.jempi.api.keyCloak;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.common.crypto.CryptoIntegration;
import org.keycloak.util.SystemPropertiesJsonParserFactory;

import java.io.IOException;
import java.io.InputStream;

public class AkkaKeycloakDeploymentBuilder extends KeycloakDeploymentBuilder {

   protected AkkaKeycloakDeploymentBuilder() {
   }

   public static KeycloakDeployment build(final InputStream is) {
      CryptoIntegration.init(org.keycloak.adapters.KeycloakDeploymentBuilder.class.getClassLoader());
      AkkaAdapterConfig adapterConfig = loadAdapterConfig(is);
      return (new AkkaKeycloakDeploymentBuilder()).internalBuild(adapterConfig);
   }

   public static AkkaAdapterConfig loadAdapterConfig(final InputStream is) {
      ObjectMapper mapper = new ObjectMapper(new SystemPropertiesJsonParserFactory());
      mapper.setSerializationInclusion(Include.NON_DEFAULT);

      try {
         AkkaAdapterConfig adapterConfig = mapper.readValue(is, AkkaAdapterConfig.class);
         return adapterConfig;
      } catch (IOException var4) {
         throw new RuntimeException(var4);
      }
   }

   public static KeycloakDeployment build(final AkkaAdapterConfig adapterConfig) {
      return (new AkkaKeycloakDeploymentBuilder()).internalBuild(adapterConfig);
   }

}
