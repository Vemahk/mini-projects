package me.vem.art;

import java.awt.image.BufferedImage;
import java.util.Queue;

import me.vem.art.struc.OpenPixelList;
import me.vem.art.struc.OpenPixelList.OpenPixelNode;

public class Pixel {
    
    private static int WIDTH, HEIGHT;
    private static Pixel[][] board;
    
    public static void build(int width, int height) {
        if(width != WIDTH || height != HEIGHT) {
            board = new Pixel[WIDTH = width][HEIGHT = height];
            for(int x=0;x<width;x++) 
                for(int y=0;y<height;y++) 
                    board[x][y] = new Pixel(x, y);
        }else
            for(int x=0;x<width;x++) 
                for(int y=0;y<height;y++) 
                    board[x][y].resetQuiet();
        
        OpenPixelList.getInstance().clear();
        System.gc();
    }
    
    public static Pixel getPixel(int x, int y) {
        if(x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT)
            return null;
        
        return board[x][y];
    }
    
    public static Queue<Pixel> getNClosestOpenPixels(int n){
        
    }
    
    private short x, y;
    private RGB color;
    
    private short occupiedNeighbors, sumR, sumG, sumB;
    
    private OpenPixelNode openNode;
    
    public Pixel(int x, int y) {
        this.x = (short)x;
        this.y = (short)y;
    }
    
    public synchronized boolean setColor(RGB color) {
        if(isSet()) return false;
        this.color = color;
        
        
        for(Dir dir : Dir.vals) {
            Pixel neighbor = getPixel(x + dir.dx, y + dir.dy);
            if(neighbor != null && !neighbor.isSet())
                if(neighbor.addNeighbor(color)) {
                    
                }
                    
        }
        
        drawTo(Fractle.image);
        return true;
    }
    
    public void setListNode(OpenPixelNode node) {
        this.openNode = node;
    }
    
    public OpenPixelNode getNode() {
        return openNode;
    }
    
    public boolean isSet() {
        return color != null;
    }
    
    /**
     * @param neighborColor
     * @return returns whether this pixel is newly open.
     */
    public boolean addNeighbor(RGB neighborColor) {
        occupiedNeighbors++;
        sumR += neighborColor.getR();
        sumG += neighborColor.getG();
        sumB += neighborColor.getB();
        
        drawTo(Fractle.image);
        
        return color == null && occupiedNeighbors == 1;
    }
    
    public int getRGB() {
        if(isSet())
            return color.getRGB();
        
        return (getR() << 16) | (getG() << 8) | getB();
    }
    
    public int distSqrTo(RGB color) {
        int dr = getR() - color.getR(),
            dg = getG() - color.getG(),
            db = getB() - color.getB();
        
        return dr * dr + dg * dg + db * db;
    }
    
    private void drawTo(BufferedImage image) {
        image.setRGB(x, y, getRGB());
    }
    
    private int getR() {
        if(isSet())
            return color.getR();
        
        return (sumR / occupiedNeighbors) & 0xFF;
    }
    
    private int getG() {
        if(isSet())
            return color.getG();
        
        return (sumG / occupiedNeighbors) & 0xFF;
    }
    
    private int getB() {
        if(isSet())
            return color.getB();
        
        return (sumB / occupiedNeighbors) & 0xFF;
    }
    
    private void resetQuiet() {
        color = null;
        occupiedNeighbors = sumR = sumG = sumB = 0;
    }
}

enum Dir{
    UL(-1, -1),
    U (0,  -1),
    UR(1,  -1),
    R (1,   0),
    DR(1,   1),
    D (0,   1),
    DL(-1,  1),
    L (-1,  0);
    
    public static final Dir[] vals = Dir.values();
    
    int dx, dy;
    
    private Dir(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }
}