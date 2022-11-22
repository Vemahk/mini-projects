package me.vem.art.graphics.preview;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import me.vem.art.async.ThreadedPrinter;
import me.vem.art.graphics.BufferedCanvas;

public class PreviewCanvas extends BufferedCanvas {

    private static final long serialVersionUID = 7955244638477501820L;
    
    private final Preview preview;
    private final BufferedImage image;
    
    public PreviewCanvas(Preview preview, int width, int height) {
        super(preview.getFrame(), new Dimension(512, 512));
        
        this.preview = preview;
        
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ThreadedPrinter.logAsync("Bitmap created");
    }
    
    @Override
    public void render(Graphics g) {
        Point pos = preview.getPos();
        int scaleDiv = preview.getScaleDiv();
        
        g.drawImage(image, pos.x, pos.y, image.getWidth() / scaleDiv, image.getHeight() / scaleDiv, preview.getFrame());
        
        g.setColor(Color.RED);
        String posStr = String.format("(%d,%d)", pos.x, pos.y);
        g.drawString(posStr, 0, 512);
    }

    void setRGB(int x, int y, int rgb) {
        this.image.setRGB(x, y, rgb);
    }
    
    void save() {
        try {
            saveImage(image);
        } catch (IOException e) { e.printStackTrace(); }
    }
    
    //Directory where the program will save the image once done.
    private static final File SAVE = new File("saves\\");
    
    /**
     * Saves the given BufferedImage to a file.
     * @param image
     * @throws IOException
     */
    public static void saveImage(BufferedImage image) throws IOException {
        if(!SAVE.exists()) SAVE.mkdirs();
        
        File outFile = new File(SAVE, "fract01.png");
        for(int i=2;outFile.exists();i++)
            outFile = new File(SAVE, String.format("fract%02d.png", i));
        ThreadedPrinter.logAsync("Out file: "+outFile.getName());
        ImageIO.write(image, "png", outFile);
        
        ThreadedPrinter.logAsync("Image written");
    }
}
