package com.chakraytest.apiusers.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

import com.chakraytest.apiusers.config.Config;


@Service
public class EncryptionService {
    private final Config config;

    public EncryptionService(Config config) {
        this.config = config;
    }

    public String encrypt(String password) {
        try {
            byte[] bytes = hexStringToByteArray(config.getSecretKey());
            SecretKeySpec secretKeySpec = new SecretKeySpec(bytes, "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encryptedPassword = cipher.doFinal(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedPassword);

        } catch (Exception e) {
            throw new RuntimeException("Error encrypting password", e);
        }
    }

    private byte[] hexStringToByteArray(String hex){
        int length = hex.length();
        byte[] bytes = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i+1), 16));
        }
        return bytes;
    }
}
