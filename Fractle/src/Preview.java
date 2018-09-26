import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

public class Preview {

	private static Point pos = new Point(0, 0);
	public static int scaleDiv = 1;
	
	private static JFrame preview;
	public static void build() {
		preview = new JFrame("Fractle Preview");
		preview.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel panel = new JPanel() {
			private static final long serialVersionUID = -5197114419981121255L;

			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				if(Art.image == null) return;
				g.drawImage(Art.image, pos.x, pos.y, Art.WIDTH / scaleDiv, Art.HEIGHT / scaleDiv, preview);
			}
		};
		panel.setPreferredSize(new Dimension(512, 512));
		panel.setFocusable(true);
		preview.setContentPane(panel);
		preview.pack();
		preview.setLocationRelativeTo(null);
		preview.setVisible(true);
		
		panel.requestFocus();
		
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
		
		panel.addKeyListener(input);
		panel.addMouseListener(input);
		panel.addMouseMotionListener(input);
		panel.addMouseWheelListener(input);
		
		new Thread(() -> {
			while(Art.alive) {
				preview.repaint();
				try { Thread.sleep(17); } catch (InterruptedException e) { }
			}
		}, "Fractle Repaint Thread").start();
	}
	
	public static void dispose() {
		preview.dispose();
		Art.repeat = false;
		Art.alive = false;
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