package me.vem.art.graphics;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.event.MouseInputListener;

import me.vem.art.Fractle;

public class Preview {

	private static Point pos = new Point(0, 0);
	public static int scaleDiv = 1;
	
	private static JFrame preview;
	
	public static void build() {
		preview = new JFrame("Fractle Preview");
		preview.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		preview.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				Preview.dispose();
			}
		});
		
		BufferedCanvas canvas = new BufferedCanvas(preview, new Dimension(512, 512)) {
			private static final long serialVersionUID = 3212996678815410337L;

			public void render(Graphics g) {
				if(Fractle.image == null)
					return;
				
				g.drawImage(Fractle.image, pos.x, pos.y, Fractle.WIDTH / scaleDiv, Fractle.HEIGHT / scaleDiv, preview);
			}
		};
		
		canvas.setFocusable(true);
		preview.setLocationRelativeTo(null);
		preview.setVisible(true);
		
		canvas.requestFocus();
		
		InputAdapter input = new InputAdapter() {
			@Override public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ESCAPE) 
					Preview.dispose();
			}
			
			private Point mouseRelPos;
			@Override public void mousePressed(MouseEvent e) {
				mouseRelPos = e.getPoint();
			}
			
			@Override public void mouseDragged(MouseEvent e) {
				if(mouseRelPos == null)
					mouseRelPos = e.getPoint();
				
				Point dPos = new Point(e.getX() - mouseRelPos.x, e.getY() - mouseRelPos.y);
				pos.translate(dPos.x, dPos.y);
				mouseRelPos = e.getPoint();
			}

			private long last = 0;
			private int scaleDivOld = 1;
			@Override public void mouseReleased(MouseEvent e) {
				mouseRelPos = null;
				if(e.getButton() == MouseEvent.BUTTON1) {
					long cur = System.currentTimeMillis();
					if(cur - last < 250) { //Double click
						int tmp = scaleDiv;
						scaleDiv = scaleDivOld;
						scaleDivOld = tmp;
						
						float mult = scaleDivOld / (float) scaleDiv;
						Point clickPos = e.getPoint();
						Point newPixPoint = new Point((int)Math.floor((clickPos.x - pos.x) * mult),
													  (int)Math.floor((clickPos.y - pos.y) * mult));
						pos = new Point(clickPos.x - newPixPoint.x, clickPos.y - newPixPoint.y);
					}
					last = cur;
				}
			}
		};
		
		canvas.addKeyListener(input);
		canvas.addMouseListener(input);
		canvas.addMouseMotionListener(input);
		canvas.addMouseWheelListener(input);
		
		new Thread(() -> {
			while(Fractle.alive) {
				long dt = canvas.render();
				
				long sleep = 17 - dt;
				if(sleep > 0) try { Thread.sleep(sleep); } catch (InterruptedException e) { }
			}
		}, "Fractle Repaint Thread").start();
	}
	
	public static void dispose() {
		preview.setVisible(false);
		preview.dispose();
		Fractle.repeat = false;
		Fractle.alive = false;
	}
}

class InputAdapter implements KeyListener, MouseInputListener, MouseWheelListener{
	@Override public void mouseWheelMoved(MouseWheelEvent e) {}
	@Override public void mouseDragged(MouseEvent e) {}
	@Override public void mouseMoved(MouseEvent e) {}
	@Override public void mouseClicked(MouseEvent e) {}
	@Override public void mouseEntered(MouseEvent e) {}
	@Override public void mouseExited(MouseEvent e) {}
	@Override public void mousePressed(MouseEvent e) {}
	@Override public void mouseReleased(MouseEvent e) {}
	@Override public void keyPressed(KeyEvent e) {}
	@Override public void keyReleased(KeyEvent e) {}
	@Override public void keyTyped(KeyEvent e) {}
}