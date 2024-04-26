package org.jembi.jempi.shared.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Key;
import java.util.Base64;

public final class EncryptionUtil {
    private static final String ALGORITHM = "AES";
    // private static final String KEY = "jempi_encryption_token";

    private EncryptionUtil() {
    }

    public static String encrypt(
            final Object data,
            final String encryptionKey) throws Exception {
        Key key = new SecretKeySpec(encryptionKey.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] serializedData = serialize(data);
        byte[] encryptedBytes = cipher.doFinal(serializedData);
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static Object decrypt(
            final String encryptedData,
            final String encryptionKey) throws Exception {
        Key key = new SecretKeySpec(encryptionKey.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);

        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return deserialize(decryptedBytes);
    }

    private static byte[] serialize(final Object obj) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.flush();
        return bos.toByteArray();
    }

    private static Object deserialize(final byte[] bytes) throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bis);
        return ois.readObject();
    }
}
