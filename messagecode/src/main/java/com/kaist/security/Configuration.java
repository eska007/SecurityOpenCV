package com.kaist.security;

/**
 * Created by rambar on 2016-08-04.
 */
public class Configuration {
    //16 x 16 = 256 bits (2 block of plaintext for AES)
    public static final int SQUARE_SIDE_LENGTH = 32; //This value has to be multiple of 16

    public static final boolean PARITY_BIT = true;
    public static final int PADDING_SIZE = 26;
    public static final int SQUARE_SIZE = 26;

    public static final int IMAGE_WIDTH = SQUARE_SIDE_LENGTH * SQUARE_SIZE + PADDING_SIZE * 2 + (PARITY_BIT ? SQUARE_SIZE : 0);
    public static final int IMAGE_HEIGHT = SQUARE_SIDE_LENGTH * SQUARE_SIZE + PADDING_SIZE * 2;
}
