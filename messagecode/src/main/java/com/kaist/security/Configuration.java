package com.kaist.security;

/**
 * Created by rambar on 2016-08-04.
 */
public class Configuration {
    //16 x 16 = 256 bits (2 block of plaintext for AES)
    public static final int NUMBER_OF_BLOCKS = 16; //This value has to be multiple of 16

    public static final int PADDING_SIZE = 40;
    public static final int SQUARE_SIZE = 40;

    public static final int IMAGE_WIDTH = NUMBER_OF_BLOCKS * SQUARE_SIZE + PADDING_SIZE * 2;
    public static final int IMAGE_HEIGHT = NUMBER_OF_BLOCKS * SQUARE_SIZE + PADDING_SIZE * 2;
}
