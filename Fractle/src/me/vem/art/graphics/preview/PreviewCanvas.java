package me.vem.art.graphics.preview;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;

import me.vem.art.graphics.BufferedCanvas;

public class PreviewCanvas extends BufferedCanvas {

    private static final long serialVersionUID = 7955244638477501820L;
    
    private final Preview preview;
    
    public PreviewCanvas(Preview preview) {
        super(preview.getFrame(), new Dimension(512, 512));
        
        this.preview = preview;
    }
    
    @Override
    public void render(Graphics g) {
        BufferedImage image = preview.getImage();
        Point pos = preview.getPos();
        int scaleDiv = preview.getScaleDiv();
        
        g.drawImage(image, pos.x, pos.y, image.getWidth() / scaleDiv, image.getHeight() / scaleDiv, preview.getFrame());
    }

}
