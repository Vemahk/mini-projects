import java.awt.Color;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Pareto {
	
	public static boolean PAUSE = true;
	public static int UPMS = 1;

	public static void main(String[] args) throws InterruptedException {
		final int SIZE = Toolkit.getDefaultToolkit().getScreenSize().width;
		final int START_MONEY = 10;
		
		int[] data = new int[SIZE];
		Arrays.fill(data, START_MONEY);
		
		JFrame frame = createFrame(SIZE, data);
		startRenderThread(frame, 60);
		
		int uc = 0; // uc -> update count
		Random rand = new Random();
		while(true) {
			if(UPMS != 0 && !PAUSE) {
				synchronized(data) {
					int a = rand.nextInt(SIZE);
					//while(data[a] == 0) a = rand.nextInt(SIZE);
					
					int b = rand.nextInt(SIZE);
					while(data[b] == 0) b = rand.nextInt(SIZE);
					
					data[a]++;
					data[b]--;
				}
				
				if(++uc >= UPMS) {
					uc = 0;
					Thread.sleep(1);
				}
			}else Thread.sleep(1);
		}
	}
	
	public static JFrame createFrame(int size, int[] data) {
		JFrame frame = new JFrame("See Pareto?");
		frame.setUndecorated(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel display = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.setColor(Color.WHITE);
				g.drawString("UPMS: " + UPMS, 5, 15);
				synchronized(data) {
					Arrays.sort(data);
					
					//Top section
					double max = data[size-1];
					for(int i=0;i<size;i++)
						g.drawLine(i, this.getHeight()/2-1, i, this.getHeight()/2 - 1 - (int)(data[i] / max * this.getHeight()/2));
					
					//Bottom section
					int modeval = 0;
					int modec = 0;
					
					int range = data[size-1] - data[0] + 1;
					
					int[] count = new int[range];
					for(int i=0, c=0;i < size;i++) {
						if(i!=0 && data[i] != data[i-1])
							c++;
						count[c]++;
						if(modec == c) {
							modeval++;
						}else if(modec != c && count[c] > modeval) {
							modec = c;
							modeval = count[c];
						}
					}
					
					int heightrange = this.getHeight()/2 - 5;
					for(int i=0;i<range;i++) {
						
						g.fillRect(i * this.getWidth() / range, this.getHeight()/2 + 5 + (int)((1-((double)count[i] / modeval)) * heightrange) , this.getWidth() / range, this.getHeight()/2);
					}
				}
			}
		};
		display.setBackground(Color.BLACK);
		display.setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());
		
		frame.setContentPane(display);
		
		frame.addKeyListener(new KeyListener() {
			@Override public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					frame.dispose();
					System.exit(0);
				}
				if(e.getKeyCode() == KeyEvent.VK_SPACE)
					PAUSE = !PAUSE;
				if(e.getKeyCode() == KeyEvent.VK_UP)
					UPMS++;
				if(e.getKeyCode() == KeyEvent.VK_DOWN && UPMS > 0)
					UPMS--;
			}	
			
			@Override public void keyReleased(KeyEvent e) {}
			@Override public void keyTyped(KeyEvent e) {}
		});
		
		frame.setResizable(false);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		return frame;
	}
	
	public static void startRenderThread(JFrame frame, int FPS) {
		new Thread(() -> {
			while(true) {
				if(!PAUSE)
					frame.repaint();
				try {
					Thread.sleep(1000 / FPS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
}