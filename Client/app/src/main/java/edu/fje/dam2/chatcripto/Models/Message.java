package edu.fje.dam2.chatcripto.Models;


import android.os.Environment;

import java.security.PrivateKey;
import java.security.Signature;

import javax.crypto.SecretKey;

public class Message {
    private String sender;
    private String message;
    private String hash;
    private String symmetricEncryption;
    private String asymmetricEncryption;
    private byte[] digitalSignature;
    private boolean isLongPressed = false;

    public Message(String sender, String msg, SecretKey symmetricSecretKey, PrivateKey asymmetricPrivateKey,
                   PrivateKey signaturePrivateKey) throws Exception {
        this.sender = sender;
        this.message = msg;

        // Hash
        this.hash = Hash.getHash(msg);

        // Symmetric encryption
        this.symmetricEncryption = SymmetricEncryption.encrypt(msg, (SecretKey) symmetricSecretKey);

        // Asymmetric encryption
        this.asymmetricEncryption = AsymmetricEncryption.encryptMessage(msg, asymmetricPrivateKey);

        // Digital signature
        this.digitalSignature = DigitalSignature.signData(msg, signaturePrivateKey);
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public String getSymmetricEncryption() {
        return symmetricEncryption;
    }

    public String getAsymmetricEncryption() {
        return asymmetricEncryption;
    }

    public String getHash() {
        return hash;
    }

    public byte[] getDigitalSignature() {
        return digitalSignature;
    }

    public boolean isLongPressed() {
        return isLongPressed;
    }

    public void setLongPressed(boolean longPressed) {
        isLongPressed = longPressed;
    }

    public String getEncryptionText() {
        return "Hash\n" + getHash() + "\n\nSymmetric Encryption\n" + getSymmetricEncryption() +
                "\n\nAsymmetric Encryption\n" + getAsymmetricEncryption() +
                "\n\nDigital Signature\n" + getDigitalSignature();
    }
}

