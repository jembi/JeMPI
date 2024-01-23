package org.jembi.jempi.api.keyCloak;

import org.keycloak.TokenVerifier;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.rotation.AdapterTokenVerifier;
import org.keycloak.adapters.rotation.PublicKeyLocator;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.IDToken;
import org.keycloak.representations.JsonWebToken;

import java.security.PublicKey;


// Code taken from the super class  org.keycloak.adapters.rotation.AdapterTokenVerifier
// Since they are static methods we need to redeclare them here
// The reason for override is that within JeMPI keycloak has 2 adddress which it is accessed from
// 1) The frontend url (KC_FRONTEND_URL) which the ui uses, and 2) the backend url (KC_API_URL), which the api uses
// of which the default verification assumes the address are the same.
// This change also us to use the appropiate url when verifying the tokenss

public final class KeyCloakAdapterTokenVerifier extends AdapterTokenVerifier {

   public static VerifiedTokens verifyTokens(
         final String accessTokenString,
         final String idTokenString,
         final KeycloakDeployment deployment,
         final AkkaAdapterConfig keycloakConfig) throws VerificationException {
      TokenVerifier<AccessToken> tokenVerifier =
            createVerifier(accessTokenString, deployment, true, AccessToken.class, keycloakConfig);
      AccessToken accessToken = tokenVerifier.verify().getToken();

      if (idTokenString != null) {
         IDToken idToken = TokenVerifier.create(idTokenString, IDToken.class).getToken();
         TokenVerifier<IDToken> idTokenVerifier = TokenVerifier.createWithoutSignature(idToken);

         idTokenVerifier.audience(deployment.getResourceName());
         idTokenVerifier.issuedFor(deployment.getResourceName());

         idTokenVerifier.verify();
         return new VerifiedTokens(accessToken, idToken);
      } else {
         return new VerifiedTokens(accessToken, null);
      }
   }

   private static PublicKey getPublicKey(
         final String kid,
         final KeycloakDeployment deployment) throws VerificationException {
      PublicKeyLocator pkLocator = deployment.getPublicKeyLocator();

      PublicKey publicKey = pkLocator.getPublicKey(kid, deployment);
      if (publicKey == null) {
         throw new VerificationException("Didn't find publicKey for specified kid");
      }

      return publicKey;
   }

   public static <T extends JsonWebToken> TokenVerifier<T> createVerifier(
         final String tokenString,
         final KeycloakDeployment deployment,
         final boolean withDefaultChecks,
         final Class<T> tokenClass,
         final AkkaAdapterConfig keycloakConfig) throws VerificationException {
      TokenVerifier<T> tokenVerifier = TokenVerifier.create(tokenString, tokenClass);

      tokenVerifier.withDefaultChecks()
                   .realmUrl(String.format("%s/realms/%s", keycloakConfig.getFrontendKcUri(), deployment.getRealm()));

      String kid = tokenVerifier.getHeader().getKeyId();
      PublicKey publicKey = getPublicKey(kid, deployment);
      tokenVerifier.publicKey(publicKey);

      return tokenVerifier;
   }
}
