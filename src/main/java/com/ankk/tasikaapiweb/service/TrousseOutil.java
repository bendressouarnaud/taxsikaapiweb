package com.ankk.tasikaapiweb.service;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class TrousseOutil {




    // M E T H O D S :
    public String encrypt(String wordToEncrypt, String key, String aesAlgoRithm) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec seckey = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance(aesAlgoRithm);//
            cipher.init(Cipher.ENCRYPT_MODE, seckey);//
            byte[] cipherText = cipher.doFinal(wordToEncrypt.getBytes());
            return Base64.getEncoder()
                    .encodeToString(cipherText);
        }
        catch (Exception e){
            //log.error("Exception in encrypt(...) : ", e);
            return "";
        }
    }

    public String decrypt(String wordToDecrypt, String key, String aesAlgoRithm) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec seckey = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance(aesAlgoRithm);
            cipher.init(Cipher.DECRYPT_MODE, seckey);
            byte[] plainText = cipher.doFinal(Base64.getDecoder()
                    .decode(wordToDecrypt));
            return new String(plainText);
        }
        catch (Exception e){
            //log.error("Exception in decrypt(...) : ", e);
            return "";
        }
    }

}
