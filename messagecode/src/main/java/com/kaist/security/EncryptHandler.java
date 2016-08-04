package com.kaist.security;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.SecureRandom;
import java.security.cert.Extension;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by rambar on 2016-08-04.
 */
public class EncryptHandler implements ActionListener{

    private MessageCode mMessageCode;

    public EncryptHandler(MessageCode messageCode) {
        mMessageCode = messageCode;
    }

    public void actionPerformed(ActionEvent e) {
        byte pack = 0x00;
        int p = 0;

        //packing 8bit to 1byte
        for(int y = 0; y < Configuration.NUMBER_OF_COLS; y++) {
            for (int x = 0; x < Configuration.NUMBER_OF_ROWS; x++) {
                int cell = (mMessageCode.drawingPlane.gridArray[y][x] == 1)? 1: 0;
                pack |= cell << (x % 8);

                if(x % 8 == 7) {
                    mMessageCode.plaintext[p++] = pack;
                    pack = 0x00;
                }
            }
        }
        /*
        System.out.print("[Msg ]");
        printByte(mMessageCode.plaintext);

        byte[] seed = mMessageCode.jTextfield.getText().getBytes();
        byte[] secretKey = getRawKey(seed);
        */

        byte[] secretKey = new byte[16];
        for(int i=0; i < secretKey.length; i++)
            secretKey[i] = 0;

        /*
        System.out.print("[Seed]");
        printByte(seed);
        */

        System.out.print("[key ]");
        printByte(secretKey);

        mMessageCode.ciphertext = encrypt(secretKey, mMessageCode.plaintext);
        System.out.print("[Ciph]");
        printByte(mMessageCode.ciphertext);

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
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static void printByte(byte[] bytedata) {
        StringBuffer result = new StringBuffer();

        for(int i = 0; i < bytedata.length; i++)
            result.append(String.format("%02X ", bytedata[i]));

        System.out.println("Hex: " + result.toString());
    }

    public byte[] encrypt(byte[] key, byte[] plaintext)  {
        try {
            SecretKey secureKey = new SecretKeySpec(key, "AES");
            Cipher c = Cipher.getInstance("AES/ECB/NoPadding");
            c.init(Cipher.ENCRYPT_MODE, secureKey);
            return c.doFinal(plaintext);
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
