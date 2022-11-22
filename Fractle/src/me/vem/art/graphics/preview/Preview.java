package me.vem.art.graphics.preview;
import java.awt.Point;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

import me.vem.art.Fractle;

public final class Preview implements AutoCloseable{

    private final BufferedImage image;
    private final PreviewMouseAdapter mouseInput;
    
    private final Point pos = new Point(0, 0);
    private final JFrame frame;
	
    private int scaleDiv;
    
	public Preview(BufferedImage image, int initScaleDiv) {
	    
	    this.image = image;
	    this.scaleDiv = initScaleDiv;
	    
		frame = new JFrame("Fractle Preview");
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		frame.addWindowListener(new PreviewWindowAdapter(this));
		
		PreviewCanvas canvas = new PreviewCanvas(this);
		canvas.setFocusable(true);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		canvas.requestFocus();
		
		canvas.addKeyListener(new PreviewKeyAdapter(this));
		
		mouseInput = new PreviewMouseAdapter(this);
		canvas.addMouseListener(mouseInput);
		canvas.addMouseMotionListener(mouseInput);
		canvas.addMouseWheelListener(mouseInput);
		
		new Thread(() -> {
			while(Fractle.alive) {
				long dt = canvas.render();
				
				long sleep = 17 - dt;
				if(sleep > 0) try { Thread.sleep(sleep); } catch (InterruptedException e) { }
			}
		}, "Fractle Repaint Thread").start();
	}
	
	public BufferedImage getImage() {
	    return image;
	}
	
	public JFrame getFrame() {
	    return frame;
	}
	
	public int getScaleDiv() {
	    return scaleDiv;
	}
	
	/**
	 * @param scaleDiv
	 * @return the old scaleDiv
	 */
	public int setScaleDiv(int scaleDiv) {
	    int old = this.scaleDiv;
	    this.scaleDiv = scaleDiv;
	    return old;
	}
	
	public Point getPos() {
	    return pos;
	}
	
	public void close() {
		frame.setVisible(false);
		frame.dispose();
		Fractle.alive = false;
	}
}
