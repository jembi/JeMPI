package org.jembi.jempi.shared.utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.util.Base64;

public final class CryptoUtils {

   private static final String ALGORITHM = "AES/GCM/NoPadding";
   private static final int GCM_TAG_LENGTH = 128;
   private static final int GCM_IV_LENGTH = 12;

   CryptoUtils(){}

   public static String encrypt(
         final String plaintext,
         final SecretKey secretKey) throws Exception {
      Cipher cipher = Cipher.getInstance(ALGORITHM);
      byte[] iv = new byte[GCM_IV_LENGTH];
      KeyGenerator.getInstance("AES").generateKey();
      GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);

      byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes());
      byte[] combined = new byte[GCM_IV_LENGTH + encryptedBytes.length];
      System.arraycopy(iv, 0, combined, 0, GCM_IV_LENGTH);
      System.arraycopy(encryptedBytes, 0, combined, GCM_IV_LENGTH, encryptedBytes.length);

      return Base64.getEncoder().encodeToString(combined);
   }

   public static String decrypt(
         final String ciphertext,
         final SecretKey secretKey) throws Exception {
      byte[] combined = Base64.getDecoder().decode(ciphertext);
      byte[] iv = new byte[GCM_IV_LENGTH];
      System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);

      Cipher cipher = Cipher.getInstance(ALGORITHM);
      GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
      cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);

      byte[] encryptedBytes = new byte[combined.length - GCM_IV_LENGTH];
      System.arraycopy(combined, GCM_IV_LENGTH, encryptedBytes, 0, encryptedBytes.length);

      return new String(cipher.doFinal(encryptedBytes));
   }
}
