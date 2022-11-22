package me.vem.art.rgb;

import java.util.Arrays;
import java.util.Iterator;

import me.vem.art.async.ThreadedPrinter;

public class ColorWheel implements Iterator<RGB> {

    private final int size;
    
    //Sorted set of all colors.
    private final int[] colors;
    
    private int position;
    private int remaining;
    
    public ColorWheel(int colorBits) {
        
        size = 1 << (colorBits * 3);
        reset();
        
        ThreadedPrinter.logAsync("ColorWheel > Sorting SimpleColors");
        
        //Gather all x-bit colors into the array.
        final int colorBitsInv = 8 - colorBits;
        final int maxVal = 1<<colorBits;
        final SimpleColor[] simpleColors = new SimpleColor[size];
        for(int i=0, r = 0, g = 0, b = 0;;) {
            simpleColors[i++] = new SimpleColor(r<<colorBitsInv, g<<colorBitsInv, b<<colorBitsInv);
            
            if(++r == maxVal) {
                r = 0;
                if(++g == maxVal) {
                    g = 0;
                    if(++b == maxVal) { //All possible colors reached; end.
                        break; 
                    }
                }
            }
        }

        ThreadedPrinter.logAsync("ColorWheel > Beginning Color Sort");
        
        Arrays.sort(simpleColors, (a, b) -> Float.compare(a.hue, b.hue));
        
        ThreadedPrinter.logAsync("ColorWheel > Mapping to RGB");

        colors = new int[size];
        for(int i=0;i<size;i++)
            colors[i] = simpleColors[i].toInt();
        
        ThreadedPrinter.logAsyncf("ColorWheel Init Complete; %d colors", size);
    }

    public void reset() {
        position = (int)(Math.random() * size);
        remaining = size;
    }
    
    @Override
    public boolean hasNext() {
        return remaining > 0;
    }

    @Override
    public RGB next() {
        RGB next = new RGB(colors[position]);
        
        if(++position >= size)
            position = 0;
        
        remaining--;
        
        return next;
    }
}