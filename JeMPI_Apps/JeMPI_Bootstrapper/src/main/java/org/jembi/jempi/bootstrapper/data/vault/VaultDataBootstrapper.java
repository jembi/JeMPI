package org.jembi.jempi.bootstrapper.data.vault;

import io.github.jopenlibs.vault.Vault;
import io.github.jopenlibs.vault.VaultConfig;
import io.github.jopenlibs.vault.VaultException;
import io.github.jopenlibs.vault.response.LogicalResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class VaultDataBootstrapper extends VaultBootstrapper {
    private static final Logger LOGGER = LogManager.getLogger(VaultDataBootstrapper.class);
    private final Vault vault;
    private final String vaultPath = "secret/jeMPI";

    public VaultDataBootstrapper(final String configFilePath) {
        super(configFilePath);
        LOGGER.info("configFilePath in vault" + configFilePath);
        this.vault = initializeVault();
    }

    private Vault initializeVault() {
        try {
            final VaultConfig config = new VaultConfig()
                    .address("http://172.20.10.3:8200")
                    .token("root")
                    .engineVersion(2)
                    .build();
            return Vault.create(config);
        } catch (VaultException e) {
            LOGGER.error("Error initializing the vault instance", e);
        }
        return vault;
    }

    @Override
    public Boolean resetVault() {
        LOGGER.info("Resetting vault secrets");
        return this.deleteEncryptionSecrets() && this.createEncryptionSecrets();
    }

    @Override
    public Boolean createEncryptionSecrets() {
        LOGGER.info("Creating Vault secrets");
        try {
            String encryptionToken = this.generateRandomString(32);
            Map<String, Object> secrets = new HashMap<>();
            secrets.put("jempi_encryption_token", encryptionToken);

            final LogicalResponse writeResponse = vault.logical()
                    .write(vaultPath, secrets);

            LOGGER.info(writeResponse);
            return true;
        } catch (VaultException e) {
            LOGGER.error("Error creating encryption secrets", e);
            return false;
        }
    }

    @Override
    public Boolean deleteEncryptionSecrets() {
        LOGGER.info("Deleting Vault secrets");
        try {
            vault.logical().delete(vaultPath);
            return true;
        } catch (VaultException e) {
            LOGGER.error("Error deleting encryption secrets", e);
            return false;
        }
    }

    private String generateRandomString(final int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append((char) (random.nextInt(94) + 33));
        }
        return sb.toString();
    }
}
