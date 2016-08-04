package com.kaist.security;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JFrame;

public class MessageCode {
    private Image image;
    private ImageIcon imageIcon;
    private JLabel jLabel;
    private JFrame jFrame;
    private JButton jClearButton;
    private JButton jEncryptButton;

    protected byte[] ciphertext;
    protected byte[] plaintext = new byte[Configuration.NUMBER_OF_ROWS * Configuration.NUMBER_OF_COLS / 8];

    protected DrawingPlane drawingPlane = new DrawingPlane();

    public MessageCode() {
        image = new BufferedImage( Configuration.IMAGE_WIDTH, Configuration.IMAGE_HEIGHT, BufferedImage.TYPE_INT_ARGB );
        imageIcon = new ImageIcon( image );
        jLabel = new JLabel( imageIcon );

        jClearButton = new JButton("Clear");
        jClearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                drawingPlane.clearGrid();
            }
        });
        jEncryptButton = new JButton("Encrypt");
        jEncryptButton.addActionListener(new EncryptHandler(this));

        drawingPlane.setSize(Configuration.IMAGE_WIDTH, Configuration.IMAGE_HEIGHT);

        jFrame = new JFrame( "Message Code" );
        jFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        Container container = jFrame.getContentPane();
        container.setLayout( new BorderLayout() );
        container.add( jLabel, BorderLayout.EAST );
        container.add( drawingPlane, BorderLayout.WEST );

        Panel center = new Panel();
        center.setLayout(new GridLayout(2,1));
        center.add( jClearButton );
        center.add( jEncryptButton );
        container.add( center, BorderLayout.CENTER );

        jFrame.pack();
    }

    protected void paint() {
        Graphics graphics = image.getGraphics();

        //draw background
        graphics.setColor( Color.black );
        graphics.fillRect( 0, 0, Configuration.IMAGE_WIDTH, Configuration.IMAGE_HEIGHT);

        // paint marker at 4 corner
        graphics.setColor( Color.decode("0xFF00FF") );
        int xs[] = {0, Configuration.IMAGE_WIDTH - Configuration.PADDING_SIZE, 0, Configuration.IMAGE_WIDTH - Configuration.PADDING_SIZE};
        int ys[] = {0, 0, Configuration.IMAGE_HEIGHT - Configuration.PADDING_SIZE, Configuration.IMAGE_HEIGHT - Configuration.PADDING_SIZE};

        for(int i = 0; i < 4; i++)
            graphics.fillRect( xs[i], ys[i], Configuration.PADDING_SIZE, Configuration.PADDING_SIZE);

        if(ciphertext == null) return;

        // paint the black squares
        for (int y = 0; y < Configuration.NUMBER_OF_ROWS; y++ ) {
            for (int x = 0; x < Configuration.NUMBER_OF_COLS; x++) {
                if(getBit(ciphertext, y * Configuration.NUMBER_OF_COLS + x) == 1) //plaintext or ciphertext
                    graphics.setColor( Color.white );
                else
                    graphics.setColor( Color.black );

                graphics.fillRect(x * Configuration.SQUARE_SIZE + Configuration.PADDING_SIZE, y * Configuration.SQUARE_SIZE + Configuration.PADDING_SIZE, Configuration.SQUARE_SIZE, Configuration.SQUARE_SIZE);
            }
        }

        jLabel.repaint();
    }

    private int getBit(byte[] bytedata, int x) {
        int idx = x / 8;
        int bp = x % 8;
        byte b = bytedata[idx];
        int res = (0x01 & (b >> bp));
        return res;
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
