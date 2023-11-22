package org.jembi.jempi.api.keyCloak;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.api.persistance.postgres.queries.UserQueries;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.ServerRequest;
import org.keycloak.adapters.rotation.AdapterTokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;

import org.jembi.jempi.api.user.User;

public class KeyCloakAuthenticator {

    private static final Logger LOGGER = LogManager.getLogger(KeyCloakAuthenticator.class);
    private final KeycloakDeployment keycloak;
    private final AkkaAdapterConfig keycloakConfig;
    private final UserQueries userQueries;
    public KeyCloakAuthenticator(){
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream keycloakConfigStream = classLoader.getResourceAsStream("keycloak.json");
        keycloakConfig = AkkaKeycloakDeploymentBuilder.loadAdapterConfig(keycloakConfigStream);
        keycloak = AkkaKeycloakDeploymentBuilder.build(keycloakConfig);
        userQueries = new UserQueries();
    }

    private User loginWithKeycloakHandler(final OAuthCodeRequestPayload payload) {
            LOGGER.debug("loginWithKeycloak");
            LOGGER.debug("Logging in {}", payload);
            try {
                // Exchange code for a token from Keycloak
                AccessTokenResponse tokenResponse = ServerRequest.invokeAccessCodeToToken(keycloak, payload.code(),
                                                                                        keycloakConfig.getRedirectUri(),
                                                                                        payload.sessionId());
                LOGGER.debug("Token Exchange succeeded!");

                String tokenString = tokenResponse.getToken();
                String idTokenString = tokenResponse.getIdToken();

                AdapterTokenVerifier.VerifiedTokens tokens = AdapterTokenVerifier.verifyTokens(tokenString, idTokenString,
                                                                                                keycloak);
                LOGGER.debug("Token Verification succeeded!");
                AccessToken token = tokens.getAccessToken();
                LOGGER.debug("Is user already registered?");
                String username = token.getPreferredUsername();
                User user = userQueries.getUser(username);
                if (user == null) {
                    // Register new user
                    LOGGER.debug("User registration ... {}", username);
                    User newUser = User.buildUserFromToken(token);
                    user = userQueries.registerUser(newUser);
                }
                LOGGER.debug("User has signed in : {}", username);
                return user;
            } catch (VerificationException e) {
                LOGGER.error("failed verification of token: {}", e.getMessage());
            } catch (ServerRequest.HttpFailure failure) {
                LOGGER.error("failed to turn code into token");
                LOGGER.error("status from server: {}", failure.getStatus());
                if (failure.getError() != null && !failure.getError().trim().isEmpty()) {
                    LOGGER.error(failure.getLocalizedMessage(), failure);
                }
            } catch (IOException e) {
                LOGGER.error("failed to turn code into token", e);
            }
            return null;
        }

        public CompletionStage<User> askLoginWithKeycloak(final OAuthCodeRequestPayload body) {
            CompletionStage<User> stage = CompletableFuture.completedFuture(loginWithKeycloakHandler(body));
            return stage.thenApply(response -> response);
        }
}
