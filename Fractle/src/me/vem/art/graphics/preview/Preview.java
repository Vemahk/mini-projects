package me.vem.art.graphics.preview;
import java.awt.Point;

import javax.swing.JFrame;

public final class Preview extends Thread {

    private final PreviewMouseAdapter mouseInput;
    
    private final Point pos = new Point(0, 0);
    private final JFrame frame;
    private final PreviewCanvas canvas;
	
    private int scaleDiv;
    
	public Preview(int width, int height, int initScaleDiv) {
	    super("Fractle Preview Thread");
	    
	    this.scaleDiv = initScaleDiv;
	    
		frame = new JFrame("Fractle Preview");
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		frame.addWindowListener(new PreviewWindowAdapter(this));
		
		canvas = new PreviewCanvas(this, width, height);
		canvas.setFocusable(true);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		canvas.requestFocus();
		
		canvas.addKeyListener(new PreviewKeyAdapter(this));
		
		mouseInput = new PreviewMouseAdapter(this);
		canvas.addMouseListener(mouseInput);
		canvas.addMouseMotionListener(mouseInput);
		canvas.addMouseWheelListener(mouseInput);
		
		this.setDaemon(true);
	}
	
	@Override
	public void run() {
	    final long nanosPerSecond = 1_000_000_000;
	    final long nanosPerMilli = 1_000_000;
	    final long desiredFps = 60;
	    final long nanosPerFrame = nanosPerSecond / desiredFps;
	    
	    while(!isInterrupted()) {
            long dt = render();
            
            long sleep = nanosPerFrame - dt;
            if(sleep > 0) try {
                Thread.sleep(sleep / nanosPerMilli, (int)(sleep % nanosPerMilli)); 
            } catch (InterruptedException e) { return; }
        }
	}
	
	public long render() {
	    return canvas.render();
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
	
	public void setRGB(int x, int y, int rgb) {
	    this.canvas.setRGB(x, y, rgb);
	}
	
	public void save() {
	    this.canvas.save();
	}
	
	public void close() {
		frame.setVisible(false);
		frame.dispose();
		System.exit(0);
	}
}
