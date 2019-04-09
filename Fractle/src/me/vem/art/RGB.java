package me.vem.art;

import java.util.Arrays;
import java.util.Comparator;

public class RGB{
	
	private static int offset = 0;
	private static RGB[] allRGB;
	public static RGB getNth(int n) {
		if(allRGB == null) return null;
		return allRGB[(n + offset) % allRGB.length];
	}
	
	public static int numRGB() { return allRGB.length; }
	
	public static void buildRGBLookup(int colorBits) {
		if(allRGB == null) {
			//Sorted set of all colors.
			allRGB = new RGB[1 << (colorBits * 3)];
			offset = (int)(Math.random() * allRGB.length);
			
			int colorBitsInv = 8 - colorBits;
			final int maxVal = 1<<colorBits;
			
			//Gather all x-bit colors into the TreeSet.
			for(int i=0, r = 0, g = 0, b = 0;;) {
				allRGB[i++] = new RGB(r<<colorBitsInv, g<<colorBitsInv, b<<colorBitsInv);
				
				if(++r == maxVal) {
					r = 0;
					if(++g == maxVal) {
						g = 0;
						if(++b == maxVal) { //All possible colors reached; end.
							Arrays.sort(allRGB, new Comparator<RGB>() { 
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
		}
	}
	
	public static void resetAllRGB() {
		for(RGB rgb : allRGB)
			rgb.reset();
		offset = (int)(Math.random() * allRGB.length);
	}
	
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
		
		for(Dir dir : Dir.vals) {
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
				dirs[i++] = Dir.vals[x];
		
		Arrays.sort(dirs, new Comparator<Dir>() {
			@Override public int compare(Dir a, Dir b) {
				int openAtA = 0;
				int openAtB = 0;
				for(Dir dir : Dir.vals) {
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
	
	public static final Dir[] vals = Dir.values();
	
	int dx, dy, bn;
	
	private Dir(int dx, int dy, int bn) {
		this.dx = dx;
		this.dy = dy;
		this.bn = bn;
	}

	public Dir opp() { return vals[(bn+4)%8]; }
}