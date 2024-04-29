package org.jembi.jempi.shared.utils;

import io.github.jopenlibs.vault.Vault;
import io.github.jopenlibs.vault.VaultConfig;
import io.github.jopenlibs.vault.response.LogicalResponse;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public final class VaultUtils {
   public static String vaultPath = "secret/jeMPI";

   VaultUtils(){}

   public static SecretKeySpec retrieveSecretKey(
         final String vaultAddress,
         final String vaultToken)
         throws Exception {

      final VaultConfig config = new VaultConfig()
            .address("http://127.0.0.1:8200")
            .token("root")
            .engineVersion(2)
            .build();

      Vault vault = Vault.create(config);

      LogicalResponse response = vault.logical().read(vaultPath);
      String keyBase64 = response.getData().get("jempi_encryption_token");
      byte[] keyBytes = Base64.getDecoder().decode(keyBase64);

      return new SecretKeySpec(keyBytes, "AES");
      // return keyBase64;

   }
}
