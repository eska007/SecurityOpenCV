package com.kaist.security;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JFrame;

public class MessageCode
{
    //16 x 16 = 256 bits (2 block of plaintext for AES)
    private int numberOfRows = 16;
    private int numberOfCols = 16;

    private int paddingSize = 40;
    private int squareSize = 40;

    private int imageWidth = numberOfRows * squareSize + paddingSize * 2;
    private int imageHeight = numberOfCols * squareSize + paddingSize * 2;

    /* For this use case, we may reasonably make the objects below attributes of
     * the CheckerBoard class. However, if this class is used by other classes,
     * some or all of the attributes below probably would best be thought of as
     * NOT being attributes of this class.
     */
    private Image image;
    private ImageIcon imageIcon;
    private JLabel jLabel;
    private JFrame jFrame;

    public MessageCode() {
        image = new BufferedImage( imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB );
        imageIcon = new ImageIcon( image );
        jLabel = new JLabel( imageIcon );
        jFrame = new JFrame( "Message Code" );
        jFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        Container container = jFrame.getContentPane();
        container.setLayout( new BorderLayout() );
        container.add( jLabel, BorderLayout.CENTER );
        jFrame.pack();
    }

    private void paint() {
        Graphics graphics = image.getGraphics();

        //draw background
        graphics.setColor( Color.black );
        graphics.fillRect( 0, 0, imageWidth, imageHeight);

        // paint marker at 4 corner
        Color color = Color.decode("0xFF00FF");
        int xs[] = {0, imageWidth - paddingSize, 0, imageWidth - paddingSize};
        int ys[] = {0, 0, imageHeight - paddingSize, imageHeight - paddingSize};

        for(int i = 0; i < 4; i++) {
            graphics.setColor( color );
            graphics.fillRect( xs[i], ys[i], paddingSize, paddingSize);
        }

        // paint the black squares
        for ( int row = 0; row < numberOfRows; row++ ) {
            for ( int col = 0; col < numberOfCols; col ++ ) {
                if((row + col) % 2 == 0)
                    graphics.setColor( Color.black );
                else
                    graphics.setColor( Color.white );

                graphics.fillRect( row * squareSize + paddingSize, col * squareSize + paddingSize, squareSize, squareSize );
            }
        }
    }

    private void view() {
        jFrame.setVisible( true );
    }

    public static void main(String[] args) {
        MessageCode messageCode = new MessageCode();
        messageCode.paint();
        messageCode.view();
    }
}
