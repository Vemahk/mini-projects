import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import javax.imageio.ImageIO;

public class Art {
	public static int WIDTH = 1;
	public static int HEIGHT = 1;
	
	//Directory where the program will save the image once done.
	public static final File SAVE = new File("saves\\");
	
	public static RGB[] colors;
	public static BufferedImage image;
	public static boolean alive = true;
	
	public static Random rand = new Random();
	
	/* ARGS */
	public static boolean save;
	public static boolean repeat;
	public static byte colorBits = 6;
	public static byte NUM_START = 1;
	
	public static boolean parseArgs(String... args) {
		for(int i=0;i<args.length;i++) {
			if("-repeat".equals(args[i]))
				repeat = true;
			if("-bits".equals(args[i]) && args.length > i + 1)
				try {
					colorBits = Byte.parseByte(args[i+1]);
					switch(colorBits) {
					case 8: Preview.scaleDiv <<= 1;
					case 7: Preview.scaleDiv <<= 2;
					default: break;
					}
				}catch (NumberFormatException e) { e.printStackTrace(); }
			if("-save".equals(args[i]))
				save = true;
			if("-help".equals(args[i])) {
				System.out.printf("Available commands:%n%s%n%s%n%s%n%s%n", "-repeat", "-bits [num 4-8]", "-save", "-numstart [num 1-127]");
				return true;
			}
			if("-numstart".equals(args[i]) && args.length > i + 1)
				try {
					NUM_START = Byte.parseByte(args[i+1]);
				}catch(NumberFormatException e) {e.printStackTrace();}
		}
		return false;
	}
	
	public static void main(String... args) throws IOException, InterruptedException {
		
		if(parseArgs(args)) return;
		
		int colorBitsInv = 8 - colorBits;
		final int maxVal = 1<<colorBits;
		
		WIDTH <<= (colorBits*3+1)/2;
		HEIGHT <<= colorBits*3 / 2;
		
		//Sorted set of all colors.
		colors = new RGB[1 << (colorBits * 3)];
		
		System.out.printf("WIDTH %d | HEIGHT %d%n", WIDTH, HEIGHT);
		
		//Gather all x-bit colors into the TreeSet.
		for(int i=0, r = 0, g = 0, b = 0;;) {
			colors[i++] = new RGB(r<<colorBitsInv, g<<colorBitsInv, b<<colorBitsInv);
			
			if(++r == maxVal) {
				r = 0;
				if(++g == maxVal) {
					g = 0;
					if(++b == maxVal) { //All possible colors reached; end.
						Arrays.sort(colors, new Comparator<RGB>() { 
							@Override public int compare(RGB o1, RGB o2) {
								if(o1.getHue() != o2.getHue())
									return o1.getHue() < o2.getHue() ? 1 : -1;
								return o1.getRGB() - o2.getRGB();
							}
						});
						System.out.printf("Array built | %d colors%n", i);
						break; 
					}
				}
			}
		}

		//Image that will be drawn to.
		image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		
		//Builds the JFrame to display the preview of the image's construction.
		Preview.build();
		
		System.out.println("Frame built.");
		
		do {
			Thread genthread = new Thread(() -> {
				//Builds the matrix to store the set of Pos objects that will handle which pixels are open.
				RGB.build(WIDTH, HEIGHT);
				for(int i=0;i<colors.length;i++)
					colors[i].reset();
				System.out.println("\nBitmap created");
				
				//The set of all open pixels. A pixel is defined as open if it is set and has a nearby unset pixel.
				LinkedList<RGB> open = new LinkedList<>();
				
				//Places the first pixel randomly from which the rest of the image builds.
				int i=0;
				for(int x=0;x<NUM_START;x++)
					open.add(colors[i++].setPos(rand.nextInt(WIDTH), rand.nextInt(HEIGHT)));
				//Iteration time!
				for(;i < colors.length && colors[i] != null;) {
					if(!alive) {
						System.out.println("Interrupted");
						break;
					}
					
					RGB next = colors[i++];
					Iterator<RGB> rIter = open.iterator();
					
					RGB closest = null;
					
					while(rIter.hasNext()) {
						RGB rNext = rIter.next();
						if(!rNext.isOpen()) rIter.remove();
						else if(closest == null)
							closest = rNext;
						else if(rNext.distSqr(next) < closest.distSqr(next))
							closest = rNext;
					}
					closest.setRandomly(next);
					
					if(next.isOpen())
						open.add(next);
				}
				
				System.out.println("Image built");
				
				try {
					if(save) {
						saveImage(image);
						System.out.println("Image written");
					}
				} catch (IOException e) { e.printStackTrace(); }
			}, "Generation Thread");
			genthread.start();
			genthread.join();
		}while(repeat);
		
		//alive = false;
	}
	
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
		System.out.println("Out file: "+outFile.getName());
		ImageIO.write(image, "png", outFile);
	}
	
}

class RGB{
	private static RGB[][] board;
	public static void build(int WIDTH, int HEIGHT) {
		board = new RGB[WIDTH][HEIGHT];
	}
	
	private static boolean isPosSet(int x, int y) {
		if(x < 0 || x >= board.length || y < 0 || y >= board[x].length) return true;
		return board[x][y] != null;
	}
	
	private int rgb;
	private float hue;
	
	private short x, y;
	private byte status, numOpen;
	
	public RGB(int r, int g, int b) {
		rgb = (r&0xFF)<<16 | (g&0xFF)<<8 | (b&0xFF);
		hue = getHue(r,g,b);
		reset();
	}
	
	public void reset() {
		x = y = -1;
		numOpen=8;
		status = 0;
	}
	
	public void setNeighbor(Dir d) { status |= 1 << d.bn; numOpen--; }
	public float getHue() { return hue; }
	public int getRGB() { return rgb; }
	public int getR() { return (rgb >>> 16) & 0xFF; }
	public int getG() { return (rgb >>> 8) & 0xFF; }
	public int getB() { return rgb & 0xFF; }
	
	public boolean isSet() { return x >= 0 && y >= 0; }
	public boolean isOpen() { return isSet() && status != -1; }
	
	public int distSqr(RGB rgb) {
		int dr = getR() - rgb.getR();
		int dg = getG() - rgb.getG();
		int db = getB() - rgb.getB();
		return dr * dr + dg * dg + db * db;
	}
	
	public RGB setPos(int x, int y) {
		this.x = (short)x;
		this.y = (short)y;
		board[x][y] = this;
		Art.image.setRGB(x, y, getRGB());
		
		for(Dir dir : Dir.vals()) {
			int nx = x + dir.dx;
			int ny = y + dir.dy;
			if(nx < 0 || nx >= Art.WIDTH || ny < 0 || ny >= Art.HEIGHT) {
				setNeighbor(dir);
				continue;
			}
			if(board[nx][ny] != null) {
				board[nx][ny].setNeighbor(dir.opp());
				setNeighbor(dir);
			}
		}
		
		return this;
	}
	
	public void setRandomly(RGB rgb) {
		Dir[] dirs = new Dir[numOpen];
		
		for(int x = 0, i=0;x < 8;x++)
			if(((status>>>x)&1) == 0)
				dirs[i++] = Dir.vals()[x];
		
		Arrays.sort(dirs, new Comparator<Dir>() {
			@Override public int compare(Dir a, Dir b) {
				int openAtA = 0;
				int openAtB = 0;
				for(Dir dir : Dir.vals()) {
					if(!isPosSet(x + a.dx + dir.dx, y + a.dy + dir.dy)) openAtA++;
					if(!isPosSet(x + b.dx + dir.dx, y + b.dy + dir.dy)) openAtB++;
				}
				return openAtA - openAtB;
			}
		});
		
		int i=0;
		for(;i < dirs.length-1 && Math.random() > .8;i++); //Favor the lower indexes heavily.
		rgb.setPos(x + dirs[i].dx, y + dirs[i].dy);
	}
	
	private static float getHue(int r, int g, int b) {
		float min = Math.min(Math.min(r, g), b);
		float max = Math.max(Math.max(r, g), b);
		
		if(min == max) return 0;
		
		float hue = 0;
		if (r == max) hue = (g - b) / (max - min);
		else if (g == max) hue = 2f + (b - r) / (max - min);
		else hue = 4f + (r - g) / (max - min);
		
		hue *= 60f;
		if(hue < 0) hue += 360;
		return hue;
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
	
	private static Dir[] vals = Dir.values();
	public static Dir[] vals() { return vals; }
	
	int dx, dy, bn;
	
	private Dir(int dx, int dy, int bn) {
		this.dx = dx;
		this.dy = dy;
		this.bn = bn;
	}

	public Dir opp() { return Dir.vals()[(bn+4)%8]; }
}