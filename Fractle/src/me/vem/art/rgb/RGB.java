package me.vem.art.rgb;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Comparator;

public class RGB{
	
	private static RGB[][] board;
	public static void build(int WIDTH, int HEIGHT) {
		board = new RGB[WIDTH][HEIGHT];
	}
	
	private static boolean isPosSet(int x, int y) {
		if(x < 0 || x >= board.length || y < 0 || y >= board[x].length) return true;
		return board[x][y] != null;
	}
	
	private final int rgb;
	private short x, y;
	private byte status, numOpen;
	
	public RGB(int r, int g, int b) {
		rgb = (r&0xFF)<<16 | (g&0xFF)<<8 | (b&0xFF);
		reset();
	}
	
	public RGB(int rgb) {
	    this.rgb = rgb;
	    reset();
	}
	
	public void reset() {
		x = y = -1;
		numOpen = 8;
		status = 0;
	}
	
	public void setNeighbor(Dir d) { status |= 1 << d.bn; numOpen--; }
	public int getRGB() { return rgb; }
	public int getR() { return (rgb >>> 16) & 0xFF; }
	public int getG() { return (rgb >>> 8) & 0xFF; }
	public int getB() { return rgb & 0xFF; }
	
	public boolean isSet() { return x >= 0 && y >= 0; }
	public boolean isOpen() { return isSet() && status != -1; }
	
	public RGB setPos(int x, int y, BufferedImage image) {
		this.x = (short)x;
		this.y = (short)y;
		board[x][y] = this;
		image.setRGB(x, y, getRGB());
		
		for(Dir dir : Dir.vals) {
			if(hasNeighbor(dir))
			    setNeighbor(dir);
		}
		
		return this;
	}
	
	public void setRandomly(RGB rgb, BufferedImage image) {
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
		rgb.setPos(x + dirs[i].dx, y + dirs[i].dy, image);
	}
	
	private boolean hasNeighbor(Dir dir) {
        int nx = x + dir.dx;
        if(nx < 0 || nx >= board.length) {
            return true;
        }
        
        RGB[] row = board[nx];
        
        int ny = y + dir.dy;
        if(ny < 0 || ny >= row.length) {
            return true;
        }
        
        if(row[ny] != null) {
            row[ny].setNeighbor(dir.opp());
            return true;
        }
        
        return false;
	}
	
	public static int distSqr(RGB a, RGB b) {
        int dr = a.getR() - b.getR();
        int dg = a.getG() - b.getG();
        int db = a.getB() - b.getB();
        return dr * dr + dg * dg + db * db;
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