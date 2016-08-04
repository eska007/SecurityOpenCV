package com.kaist.security;

/**
 * Created by rambar on 2016-08-04.
 */
public class Configuration {
    //16 x 16 = 256 bits (2 block of plaintext for AES)
    public static final int NUMBER_OF_ROWS = 16;
    public static final int NUMBER_OF_COLS = 16;

    public static final int PADDING_SIZE = 40;
    public static final int SQUARE_SIZE = 40;

    public static final int IMAGE_WIDTH = NUMBER_OF_ROWS * SQUARE_SIZE + PADDING_SIZE * 2;
    public static final int IMAGE_HEIGHT = NUMBER_OF_COLS * SQUARE_SIZE + PADDING_SIZE * 2;
}
