package me.vem.art.rgb;

public class SimpleColor {
    public final int r, g, b;
    public final float hue;
    
    public SimpleColor(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.hue = getHue(r,g,b);
    }
    
    public int toInt() {
        return (r&0xFF)<<16 | (g&0xFF)<<8 | (b&0xFF);
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
