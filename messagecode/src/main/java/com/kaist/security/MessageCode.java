package com.kaist.security;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.ImageIcon;
import javax.swing.JButton;
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

    private Image image;
    private ImageIcon imageIcon;
    private JLabel jLabel;
    private JFrame jFrame;
    private JButton jClearButton;
    private JButton jEncryptButton;

    private byte[] ciphertext;
    private byte[] plaintext = new byte[32];

    private boolean gridArray[][] = new boolean[numberOfCols][numberOfRows];

    public MessageCode() {
        image = new BufferedImage( imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB );
        imageIcon = new ImageIcon( image );
        jLabel = new JLabel( imageIcon );

        jClearButton = new JButton("Clear");
        jClearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearGrid();
            }
        });
        jEncryptButton = new JButton("Encrypt");
        jEncryptButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                byte pack = 0x00;
                int p = 0;
                for(int y = 0; y < numberOfCols; y++) {
                    for (int x = 0; x < numberOfRows; x++) {
                        int cell = gridArray[y][x]? 1: 0;
                        pack |= cell << (x % 8);

                        if(x % 8 == 7) {
                            plaintext[p++] = pack;
                            pack = 0x00;
                        }
                    }
                }
                printByte(plaintext);

                byte[] secretKey = new byte[16];
                for(int i=0; i < secretKey.length; i++)
                    secretKey[i] = 0;

                ciphertext = encrypt(secretKey, plaintext);
                printByte(ciphertext);

                paintCode();
            }
        });

        canvas.setSize(imageWidth, imageHeight);

        jFrame = new JFrame( "Message Code" );
        jFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        Container container = jFrame.getContentPane();
        container.setLayout( new BorderLayout() );
        container.add( jLabel, BorderLayout.EAST );
        container.add( jClearButton, BorderLayout.NORTH );
        container.add( jEncryptButton, BorderLayout.SOUTH );
        container.add( canvas, BorderLayout.WEST );
        jFrame.pack();
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

    public void clearGrid() {
        for(int y = 0; y < numberOfCols; y++)
            for(int x = 0; x < numberOfRows; x++)
                gridArray[y][x] = false;
        canvas.repaint();
    }

    private Canvas canvas = new Canvas (){
        MouseAdapter mouseAdapter = new MouseAdapter() {
            private boolean setGrid(int x, int y) {
                boolean needRepaint = false;
                int xIndex = (x - paddingSize) / squareSize;
                int yIndex = (y - paddingSize) / squareSize;

                if(xIndex < 0 || xIndex >= numberOfRows) return false;
                if(yIndex < 0 || yIndex >= numberOfCols) return false;

                if(gridArray[yIndex][xIndex]) {
                    needRepaint = false;
                } else {
                    gridArray[yIndex][xIndex] = true;
                    needRepaint = true;
                }
                return needRepaint;
            }

            public void mousePressed ( MouseEvent e ) {
                if(setGrid(e.getPoint().x, e.getPoint().y))
                    repaint ();
            }

            public void mouseDragged ( MouseEvent e ) {
                if(setGrid(e.getPoint().x, e.getPoint().y))
                    repaint ();
            }
        };

        {
            addMouseListener(mouseAdapter);
            addMouseMotionListener(mouseAdapter);
        }

        public void paint( Graphics graphics ) {
            int width = getSize().width;
            int height = getSize().height;

            graphics.setColor( Color.black );
            graphics.fillRect(0, 0, width, height);

            int x[] = new int[] {paddingSize, width - paddingSize, width - paddingSize, paddingSize};
            int y[] = new int[] {paddingSize, paddingSize, height - paddingSize, height - paddingSize};

            graphics.setColor( Color.white );
            for(int i = 0; i < x.length; i++)
                graphics.drawLine(x[i], y[i], x[(i + 1) % 4], y[(i + 1) % 4]);

            for ( int row = 0; row < numberOfRows; row++ ) {
                for ( int col = 0; col < numberOfCols; col++ ) {
                    if(gridArray[col][row])
                        graphics.fillRect( row * squareSize + paddingSize, col * squareSize + paddingSize, squareSize, squareSize );
                }
            }
        }
    };

    private void paintCode() {
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

        if(ciphertext == null) return;

        // paint the black squares
        for ( int y = 0; y < numberOfRows; y++ ) {
            for (int x = 0; x < numberOfCols; x++) {
                if(getBit(ciphertext, y * numberOfCols + x) == 1)
                //if(getBit(plaintext, y * numberOfCols + x) == 1)
                    graphics.setColor( Color.white );
                else
                    graphics.setColor( Color.black );

                graphics.fillRect(x * squareSize + paddingSize, y * squareSize + paddingSize, squareSize, squareSize);
            }
        }

        jLabel.repaint();
    }

    private int getBit(byte[] bytedata, int x) {
        int idx = x / 8;
        int bp = x % 8;
        byte b = bytedata[idx];
        int res = (0x01 & (b >> bp));
        //System.out.println(res);
        return res;
    }

    private void view() {
        jFrame.setVisible( true );
    }

    public static void main(String[] args) {
        MessageCode messageCode = new MessageCode();
        messageCode.paintCode();
        messageCode.view();
    }
}
