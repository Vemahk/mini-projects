import java.awt.Toolkit;
import java.util.Arrays;
import java.util.Random;

import javax.swing.JFrame;

public class Pareto {
	
	public static boolean PAUSE = true;
	public static int UPMS = 1;

	public static void main(String[] args) throws InterruptedException {
		final int SIZE = Toolkit.getDefaultToolkit().getScreenSize().width;
		final int START_MONEY = 10;
		
		int[] data = new int[SIZE];
		Arrays.fill(data, START_MONEY);
		
		ParetoCanvas canvas = createFrame(SIZE, data);
		startRenderThread(canvas, 144);
		
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
	
	public static ParetoCanvas createFrame(int size, int[] data) {
		JFrame frame = new JFrame("See Pareto?");
		frame.setUndecorated(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		ParetoCanvas display = new ParetoCanvas(frame, data);
		frame.setVisible(true);
		
		return display;
	}
	
	public static void startRenderThread(ParetoCanvas canvas, int FPS) {
		new Thread(() -> {
			while(true) {
				if(!PAUSE && UPMS > 0) {
				    long dt = System.nanoTime();
				    canvas.render();
				    dt = (System.nanoTime() - dt) / 1000000L;
				    
				    sleep((1000L / FPS) - dt);
				}else {
				    sleep(1000L / FPS);
				}
			}
		}).start();
	}
	
	private static boolean sleep(long delay) {
	    if(delay <= 0) return false;
	    
	    try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
	    
	    return true;
	}
}