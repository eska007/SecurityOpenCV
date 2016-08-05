package com.kaist.security;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import static java.lang.System.arraycopy;

/**
 * Created by rambar on 2016-08-04.
 */
public class EncryptHandler implements ActionListener {
    private MessageCode mMessageCode;

    private byte[] plaintext;
    private byte[] ciphertext;

    public EncryptHandler(MessageCode messageCode) {
        mMessageCode = messageCode;
    }

    public void actionPerformed(ActionEvent e) {
        plaintext = new byte[Configuration.NUMBER_OF_BLOCKS * Configuration.NUMBER_OF_BLOCKS / 8];
        byte pack = 0x00;
        int p = 0;

        //packing 8bit to 1byte
        for (int y = 0; y < Configuration.NUMBER_OF_BLOCKS; y++) {
            for (int x = 0; x < Configuration.NUMBER_OF_BLOCKS; x++) {
                int cell = (mMessageCode.getDrawingPlane().getGridArray()[y][x] == 1) ? 1 : 0;
                pack |= cell << (x % 8);

                if (x % 8 == 7) {
                    plaintext[p++] = pack;
                    pack = 0x00;
                }
            }
        }

        System.out.print("[Msg ]");
        printByte(plaintext);

        byte[] secretKey = new byte[16];
        for (int i = 0; i < secretKey.length; i++) {
            secretKey[i] = '0';
        }

        byte[] registKey = mMessageCode.jTextfield.getText().getBytes();

        //arraycopy(registKey, 0, secretKey,0, 16);
        for (int i = 0; i < registKey.length; i++) {
            secretKey[i] = registKey[i];
        }

/*
        byte[] secretKey = new byte[16];
        for(int i = 0; i < secretKey.length; i++)
            secretKey[i] = 0;

        System.out.print("[Seed]");
        printByte(secretKey);
*/

        System.out.print("[key ]");
        printByte(secretKey);

        ciphertext = encrypt(secretKey, plaintext);
        System.out.print("[Ciph]");
        printByte(ciphertext);

        mMessageCode.paint();
    }

    private static byte[] getRawKey(byte[] seed) {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            SecureRandom sr;
            sr = SecureRandom.getInstance("SHA1PRNG");

            sr.setSeed(seed);
            kgen.init(128, sr); // 192 and 256 bits may not be available
            SecretKey skey = kgen.generateKey();
            byte[] raw = skey.getEncoded();
            return raw;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void printByte(byte[] bytedata) {
        StringBuffer result = new StringBuffer();

        for (int i = 0; i < bytedata.length; i++)
            result.append(String.format("%02X ", bytedata[i]));

        System.out.println("Hex: " + result.toString());
    }

    public byte[] encrypt(byte[] key, byte[] plaintext) {
        try {
            SecretKey secureKey = new SecretKeySpec(key, "AES");
            Cipher c = Cipher.getInstance("AES/ECB/NoPadding");
            c.init(Cipher.ENCRYPT_MODE, secureKey);
            return c.doFinal(plaintext);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected byte[] getCipherText() {
        return ciphertext;
    }
}
