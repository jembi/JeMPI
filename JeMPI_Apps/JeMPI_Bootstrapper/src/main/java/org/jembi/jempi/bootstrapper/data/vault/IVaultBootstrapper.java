package org.jembi.jempi.bootstrapper.data.vault;

public interface IVaultBootstrapper {
   Boolean createEncryptionSecrets();

   Boolean deleteEncryptionSecrets();

   Boolean resetVault();
}
