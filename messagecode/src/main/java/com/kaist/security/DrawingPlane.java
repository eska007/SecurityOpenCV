package com.kaist.security;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * Created by rambar on 2016-08-04.
 */
public class DrawingPlane extends Canvas implements MouseListener, MouseMotionListener{
    private int gridArray[][];

    public DrawingPlane() {
        gridArray = new int[Configuration.NUMBER_OF_BLOCKS][Configuration.NUMBER_OF_BLOCKS];

        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public void clearGrid() {
        for(int y = 0; y < Configuration.NUMBER_OF_BLOCKS; y++)
            for(int x = 0; x < Configuration.NUMBER_OF_BLOCKS; x++)
                gridArray[y][x] = 0;
        repaint();
    }

    private boolean setGrid(int x, int y) {
        boolean needRepaint = false;
        int xIndex = (x - Configuration.PADDING_SIZE) / Configuration.SQUARE_SIZE;
        int yIndex = (y - Configuration.PADDING_SIZE) / Configuration.SQUARE_SIZE;

        if(xIndex < 0 || xIndex >= Configuration.NUMBER_OF_BLOCKS) return false;
        if(yIndex < 0 || yIndex >= Configuration.NUMBER_OF_BLOCKS) return false;

        if(gridArray[yIndex][xIndex] == 1) {
            needRepaint = false;
        } else {
            gridArray[yIndex][xIndex] = 1;
            needRepaint = true;
        }
        return needRepaint;
    }

    public void mousePressed(MouseEvent e) {
        if(setGrid(e.getPoint().x, e.getPoint().y))
            repaint ();
    }

    public void mouseDragged(MouseEvent e) {
        if(setGrid(e.getPoint().x, e.getPoint().y))
            repaint ();
    }

    public void paint( Graphics graphics ) {
        int width = getSize().width;
        int height = getSize().height;

        graphics.setColor(Color.black);
        graphics.fillRect(0, 0, width, height);

        int cx[] = new int[]{Configuration.PADDING_SIZE, width - Configuration.PADDING_SIZE, width - Configuration.PADDING_SIZE, Configuration.PADDING_SIZE};
        int cy[] = new int[]{Configuration.PADDING_SIZE, Configuration.PADDING_SIZE, height - Configuration.PADDING_SIZE, height - Configuration.PADDING_SIZE};

        graphics.setColor(Color.white);
        for (int i = 0; i < cx.length; i++)
            graphics.drawLine(cx[i], cy[i], cx[(i + 1) % 4], cy[(i + 1) % 4]);

        for (int y = 0; y < Configuration.NUMBER_OF_BLOCKS; y++) {
            for (int x = 0; x < Configuration.NUMBER_OF_BLOCKS; x++) {
                if (gridArray[y][x] == 1)
                    graphics.fillRect(x * Configuration.SQUARE_SIZE + Configuration.PADDING_SIZE, y * Configuration.SQUARE_SIZE + Configuration.PADDING_SIZE, Configuration.SQUARE_SIZE, Configuration.SQUARE_SIZE);
            }
        }
    }

    protected int[][] getGridArray() {
        return gridArray;
    }

    public void mouseClicked(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) {    }
    public void mouseEntered(MouseEvent e) {    }
    public void mouseExited(MouseEvent e) {    }
    public void mouseMoved(MouseEvent e) {   }
}
