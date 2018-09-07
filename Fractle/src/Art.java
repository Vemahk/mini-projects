import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Art {

	public static Pos startingPos;
	
	public static byte colorBits = 7;
	
	public static int WIDTH = 1;
	public static int HEIGHT = 1;
	
	//Directory where the program will save the image once done.
	public static final String DESKTOP = System.getProperty("user.home") + "\\Desktop\\fract";
	
	public static TreeSet<RGB> colors;
	public static HashSet<Pos> open;
	public static BufferedImage image;
	
	public static JFrame window;
	
	public static Random rand = new Random();
	
	public static void main(String[] args) throws IOException {
		
		if(args.length > 0)
			try {
				colorBits = Byte.parseByte(args[0]);
			}catch(NumberFormatException e) { e.printStackTrace(); }
		
		int colorBitsInv = 8 - colorBits;
		final byte maxVal = (byte) (1<<colorBits);
		
		WIDTH <<= (colorBits*3+1)/2;
		HEIGHT <<= colorBits*3 / 2;
		
		//Sorted set of all colors.
		colors = new TreeSet<>();
		
		System.out.printf("WIDTH %d | HEIGHT %d%n", WIDTH, HEIGHT);
		
		//Gather all x-bit colors into the TreeSet.
		for(byte r = 0, g = 0, b = 0;;) {
			colors.add(new RGB(r<<colorBitsInv, g<<colorBitsInv, b<<colorBitsInv));
			//System.out.printf("%d %d %d%n", r, g, b);
			if(++r == maxVal) {
				r = 0;
				if(++g == maxVal) {
					g = 0;
					if(++b == maxVal)
						break; //All possible colors reached; end.
				}
			}
		}
		
		System.gc();
		System.out.printf("Tree built | %d colors%n", colors.size());
		
		//Image that will be drawn to.
		image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		
		//Builds the JFrame to display the preview of the image's construction.
		buildPreview();
		
		//Builds the matrix to store the set of Pos objects that will handle which pixels are open.
		Pos.buildStatus(WIDTH, HEIGHT);
		System.out.println("Bitmap created");
		
		//Iterator for all the colors.
		Iterator<RGB> iter = colors.iterator();
		
		//The set of all open pixels. A pixel is defined as open if it is set and has a nearby unset pixel.
		open = new HashSet<>(1 << colorBits); //So apparently setting this thing to be capable of holding large numbers speeds it up... Who'd've thunk?
		
		//Places the first pixel randomly from which the rest of the image builds.
		startingPos = Pos.status[rand.nextInt(WIDTH)][rand.nextInt(HEIGHT)];
		open.add(startingPos.setRGB(iter.next().getRGB()));

		while(iter.hasNext()) {
			int next = iter.next().getRGB();
			
			Iterator<Pos> piter = open.iterator();
			
			Pos closest = null;
			
			while(piter.hasNext()) {
				Pos pnext = piter.next();
				if(!pnext.isOpen()) piter.remove();
				else if(closest == null)
					closest = pnext;
				else if(pnext.roughDist(next) < closest.roughDist(next))
					closest = pnext;
			}

			Pos n = closest.setRandom(next);
			
			if(n == null) {//Theoretically this should never happen... I actually am not sure what needs to be done if this somehow occurs.
				open.remove(closest);
				continue; //?
			}
			
			
			if(n.isOpen())
				open.add(n);
		}
		
		System.out.println("Image built.");
		
		drawImage(image);
		
		System.out.println("Image written.");
		
		window.repaint();
	}
	
	/**
	 * Saves the given BufferedImage to a file.
	 * @param image
	 * @throws IOException
	 */
	public static void drawImage(BufferedImage image) throws IOException {
		File outFile = new File(DESKTOP + "01.png");
		for(int i=2;outFile.exists();)
			outFile = new File(DESKTOP + String.format("%02d.png", i++));
		System.out.println("Out file: "+outFile.getName());
		ImageIO.write(image, "png", outFile);
	}
	
	public static void buildPreview() {
		window = new JFrame("Progress Check");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		int renderWidth = 512;
		int renderHeight = (WIDTH == HEIGHT) ? 512 : 256;
		JPanel panel = new JPanel() {
			private static final long serialVersionUID = -5197114419981121255L;

			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.drawImage(image, 0, 0, renderWidth, renderHeight, window);
			}
		};
		panel.setPreferredSize(new Dimension(renderWidth-10, renderHeight-10));
		window.add(panel);
		
		window.pack();
		window.setResizable(false);
		window.setLocationRelativeTo(null);
		window.setVisible(true);
		
		new Thread(() -> {
			for(;;) {
				window.repaint();
				try { Thread.sleep(17); } catch (InterruptedException e) { }
			}
		}, "Fractle Repaint Thread").start();
	}
	
}

class Pos{
	
	public static Pos[][] status;
	
	public static void buildStatus(int WIDTH, int HEIGHT) {
		status = new Pos[WIDTH][HEIGHT];
		
		for(int x=0;x<WIDTH;x++)
			for(int y=0;y<HEIGHT;y++)
				status[x][y] = new Pos(x, y);
	}
	
	private short x;
	private short y;
	private int rgb;
	
	private byte neighborStatus;
	private boolean isSet;
	
	public Pos(int x, int y) {
		this.x = (short)x;
		this.y = (short)y;
	}
	
	public void setNeighborAsSet(Dir d) {
		neighborStatus |= 1 << d.bn;
	}
	
	public Pos setRGB(int rgb) {
		this.rgb = rgb;
		isSet = true;
		Art.image.setRGB(x, y, rgb);
		
		for(Dir dir : Dir.values()) {
			int nx = x + dir.dx;
			int ny = y + dir.dy;
			if(nx < 0 || nx >= Art.WIDTH || ny < 0 || ny >= Art.HEIGHT) {
				setNeighborAsSet(dir);
				continue;
			}
			status[nx][ny].setNeighborAsSet(dir.opp());
		}
		
		return this;
	}
	
	public int getRGB() { return rgb; }
	public int getR() { return rgb >>> 16; }
	public int getG() { return (rgb >>> 8) & 0xFF; }
	public int getB() { return rgb & 0xFF; }
	
	public Pos getNeighbor(Dir dir) {
		int nx = x + dir.dx;
		int ny = y + dir.dy;
		if(nx < 0 || nx >= status.length || y < 0 || y >= status[x].length)
			return null;
		return status[nx][ny];
	}
	
	public boolean isOpen() {
		return isSet && neighborStatus != -1;
	}
	
	public int roughDist(int rgb) {
		int dr = getR() - (rgb >>> 16);
		int dg = getG() - ((rgb >>> 8) & 0xFF);
		int db = getB() - (rgb & 0xFF);
		return dr * dr + dg * dg + db * db;
	}
	
	private int numOpenNeighbors(){
		int out = 0;
		for(int i = 0; i < 8; i++)
			if(((this.neighborStatus>>>i)&1) == 0) out++;
		return out;
	}
	
	/**
	 * Presumed that this is an open pixel...
	 * @param rgb
	 * @return
	 */
	public Pos setRandom(int rgb) {
		Pos[] poss = new Pos[8];
		
		for(int tmp = neighborStatus, x = 0;x < 8;tmp>>>=1, x++)
			if((tmp&1) == 0)
				poss[x] = getNeighbor(Dir.values()[x]);
		
		Arrays.sort(poss, (Pos a, Pos b) -> {
			if(a == null)
				if(b == null)
					return 0;
				else return 1;
			else if(b == null)
				return -1;
			
			return a.numOpenNeighbors() - b.numOpenNeighbors();
		});
		
		if(poss[0] == null)
			return null; //Shouldn't happen, but... ya know.
		
		int std = poss[0].numOpenNeighbors();
		int range = 7;
		
		for(;range >= 0 && poss[range] == null; range--);
		
		while(range > 0 && poss[range].numOpenNeighbors() != std)
			range--;
		
		return poss[(int)(Math.random()*(range+1))].setRGB(rgb);
	}
}

enum Dir{
	UL(-1, -1, 0),
	U (0,  -1, 1),
	UR(1,  -1, 2),
	R (1,   0, 3),
	DR(1,   1, 4),
	D (0,   1, 5),
	DL(-1,  1, 6),
	L (-1,  0, 7);
	
	byte dx, dy, bn;
	
	private Dir(int dx, int dy, int byteNum) {
		this.dx = (byte)dx;
		this.dy = (byte)dy;
		this.bn = (byte)byteNum;
	}

	public Dir opp() { return Dir.values()[(bn+4)%8]; }
}

class RGB implements Comparable<RGB>{
	
	private int rgb;
	private float hue;
	
	public RGB(int r, int g, int b) {
		rgb = (r&0xFF)<<16 | (g&0xFF)<<8 | (b&0xFF);
		
		//set the hue
		hue = getHue(r,g,b);
	}
	
	public int getRGB() { return rgb; }
	
	@Override
	public int compareTo(RGB o) {
		if(hue != o.hue)
			return hue < o.hue ? 1 : -1;
		return rgb - o.rgb;
	}
	
	private static float getHue(int r, int g, int b) {
		float min = Math.min(Math.min(r, g), b);
		float max = Math.max(Math.max(r, g), b);
		
		if(min == max) return 0;
		
		float hue = 0;
		if (r == max)
			hue = (g - b) / (max - min);
		else if (g == max)
			hue = 2f + (b - r) / (max - min);
		else
			hue = 4f + (r - g) / (max - min);
		
		hue *= 60f;
		if(hue < 0) hue += 360;
		return hue;
	}
}