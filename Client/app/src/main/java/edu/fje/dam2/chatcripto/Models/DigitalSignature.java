package edu.fje.dam2.chatcripto.Models;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;


public class DigitalSignature {
    private KeyPair keyPair;
    private PublicKey publicKey;
    private PrivateKey privateKey;

    public DigitalSignature() throws NoSuchAlgorithmException {
        KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
        this.keyPair = keygen.generateKeyPair();
        this.privateKey = this.keyPair.getPrivate();
        this.publicKey = this.keyPair.getPublic();
    }

    public static byte[] generateSignature(String msg, Signature sign) throws UnsupportedEncodingException, SignatureException, NoSuchAlgorithmException {
        byte[] data = msg.getBytes(StandardCharsets.UTF_8);
        sign.update(data);

        return sign.sign();
    }

    public static byte[] signData(String msg, PrivateKey priv) throws Exception {
        byte[] signature = null;
        byte[] data = msg.getBytes(StandardCharsets.UTF_8);

        Signature signer = Signature.getInstance("SHA1withRSA/PSS");
        signer.initSign(priv);
        signer.update(data);
        signature = signer.sign();

        return signature;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }
}
