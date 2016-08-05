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
import javax.swing.JTextField;

public class MessageCode {
    protected Image image;
    protected ImageIcon imageIcon;
    protected JLabel jLabel;
    protected JFrame jFrame;
    protected JButton jClearButton;
    protected JButton jEncryptButton;
    protected JTextField jTextfield;

    private DrawingPlane drawingPlane;
    private EncryptHandler encryptHandler;

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
        encryptHandler = new EncryptHandler(this);
        jEncryptButton.addActionListener(encryptHandler);
        jTextfield = new JTextField();

        drawingPlane = new DrawingPlane();
        drawingPlane.setSize(Configuration.IMAGE_WIDTH, Configuration.IMAGE_HEIGHT);

        jFrame = new JFrame( "Message Code" );
        jFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        Container container = jFrame.getContentPane();
        container.setLayout( new BorderLayout() );
        container.add( jLabel, BorderLayout.EAST );
        container.add( drawingPlane, BorderLayout.WEST );

        Panel panel1 = new Panel();
        Panel panel2 = new Panel();
        panel1.setLayout(new GridLayout(1,3));
        panel1.add( jClearButton );
        panel1.add( panel2 );
        panel2.setLayout(new GridLayout(2,1));
        panel2.add( new JLabel("Input Key:") );
        panel2.add( jTextfield );
        panel1.add( jEncryptButton );
        container.add( panel1, BorderLayout.SOUTH );

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

        if(encryptHandler.getCipherText() == null) return;

        // paint the black squares
        for (int y = 0; y < Configuration.NUMBER_OF_BLOCKS; y++ ) {
            for (int x = 0; x < Configuration.NUMBER_OF_BLOCKS; x++) {
                if(getBit(encryptHandler.getCipherText(), y * Configuration.NUMBER_OF_BLOCKS + x) == 1) //plaintext or ciphertext
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

    protected DrawingPlane getDrawingPlane() { return drawingPlane; }

}
