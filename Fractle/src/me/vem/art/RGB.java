package me.vem.art;

import java.util.Arrays;
import java.util.Comparator;

public class RGB{
	
	private static int start = 0;
	private static int offset = 0;
	private static RGB[] colors;

	public synchronized static boolean hasNext() {
		return offset < colors.length;
	}
	
	public synchronized static RGB getNext() {
		if(colors == null || !hasNext()) return null;
		RGB next = colors[(start + offset++) % colors.length];
		
		return next;
	}
	
	public static void buildRGBLookup(int colorBits) {
		if(colors == null) {
			//Sorted set of all colors.
			colors = new RGB[1 << (colorBits * 3)];

			start = (int)(Math.random() * colors.length);
			offset = 0;
			
			int colorBitsInv = 8 - colorBits;
			final int maxVal = 1<<colorBits;
			
			//Gather all x-bit colors into the array.
			for(int i=0, r = 0, g = 0, b = 0;;) {
				colors[i++] = new RGB(r<<colorBitsInv, g<<colorBitsInv, b<<colorBitsInv);
				
				if(++r == maxVal) {
					r = 0;
					if(++g == maxVal) {
						g = 0;
						if(++b == maxVal) { //All possible colors reached; end.
							Arrays.sort(colors, new Comparator<RGB>() { 
								@Override public int compare(RGB c1, RGB c2) {
									if(c1.getHue() != c2.getHue())
										return c1.getHue() < c2.getHue() ? 1 : -1;
									return c1.getRGB() - c2.getRGB();
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
	
	public static void resetAll() {
		start = (int)(Math.random() * colors.length);
		offset = 0;
	}
	
	private int rgb;
	private float hue;
	
	public RGB(int r, int g, int b) {
		rgb = (r&0xFF)<<16 | (g&0xFF)<<8 | (b&0xFF);
		hue = getHue(r,g,b);
	}
	
	public float getHue() { return hue; }
	public int getRGB() { return rgb; }
	public int getR() { return (rgb >>> 16) & 0xFF; }
	public int getG() { return (rgb >>> 8) & 0xFF; }
	public int getB() { return rgb & 0xFF; }
	
	public int distSqr(RGB rgb) {
		int dr = getR() - rgb.getR();
		int dg = getG() - rgb.getG();
		int db = getB() - rgb.getB();
		return dr * dr + dg * dg + db * db;
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