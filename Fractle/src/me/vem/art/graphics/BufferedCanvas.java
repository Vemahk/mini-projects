package me.vem.art.graphics;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;

public abstract class BufferedCanvas extends Canvas{
	
	private static final long serialVersionUID = 2293864682604141349L;
	
	private BufferStrategy strat;
	
	public BufferedCanvas(Frame parent, Dimension size) {
		super();
		setSize(size);
		setBackground(Color.BLACK);

		parent.add(this);
		parent.pack();
		
		createBufferStrategy(2);
		strat = getBufferStrategy();
	}
	
	/**
	 * @return the time in nanoseconds it took to render.
	 */
	public long render() {
		long start = System.nanoTime();
		do {
			do {
				Graphics graphics = strat.getDrawGraphics();
				graphics.clearRect(0, 0, getSize().width, getSize().height);
				/* Render Step */
				render(graphics);
				
				graphics.dispose();
			}while(strat.contentsRestored());
			strat.show();
		}while(strat.contentsLost());

		return System.nanoTime() - start;
	}
	
	public abstract void render(Graphics g);
}
