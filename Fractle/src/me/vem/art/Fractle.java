package me.vem.art;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import me.vem.art.async.ThreadedPrinter;
import me.vem.art.rgb.ColorBoard;
import me.vem.art.rgb.ColorChange;
import me.vem.art.rgb.ColorWheel;
import me.vem.art.struct.LimitedConcurrentQueue;

public class Fractle extends Thread {
    
	private final Random rand = new Random();
	
	private final ColorWheel colorWheel;
	
	private final int width;
	private final int height;
	private final int numStart;
	
	private final boolean repeat;
    
    public final LimitedConcurrentQueue<ColorChange> queue = new LimitedConcurrentQueue<>();
	
    private final AtomicLong productionCount = new AtomicLong(0);
    private final AtomicLong loopSize = new AtomicLong(0);
    
	public Fractle(ColorWheel colorWheel, int width, int height, int numStart, boolean repeat) {
	    this.colorWheel = colorWheel;
	    this.width = width;
	    this.height = height;
	    this.numStart = numStart;
	    this.repeat = repeat;
	}
	
	@Override
	public void run() {
        // The set of all open pixels. A pixel is defined as open if it is set and has a nearby unset pixel.
	    LinkedList<ColorChange> open = new LinkedList<ColorChange>();
	    
	    Thread logThread = new Thread(() -> {
	        while(true) {
	            try {
                    Thread.sleep(1000);
                } 
	            catch (InterruptedException e) {
                    return;
                }
	            finally {
	                long count = productionCount.getAndSet(0);
	                long loopSize = this.loopSize.getAndSet(0);
	                ThreadedPrinter.logAsyncf("Placed %d pixels. Current open size: %d. Weight: %d", count, open.size(), loopSize);
	            }
	        }
	    }) ;
	    
	    logThread.setDaemon(true);
	    logThread.start();
	    
	    try {
	        while(!isInterrupted()) {
	            loop(open);
	            if(!repeat)
	                break;
	        }
	    }
	    finally {
	        ThreadedPrinter.logAsync("Fractle Complete");
	        queue.endProduction();
	        logThread.interrupt();
	    }
	}
	
	private void loop(LinkedList<ColorChange> open) {

        //Builds the matrix to store the set of Pos objects that will handle which pixels are open.
        ColorBoard board = new ColorBoard(width, height);
        Iterator<Integer> colorIter = colorWheel.iterator();
        
        open.clear();
        
        //Places the first pixel randomly from which the rest of the image builds.
        for(int i=0;i<numStart;i++) {
            int rx = rand.nextInt(width);
            int ry = rand.nextInt(height);
            int rgb = colorIter.next().intValue();
            
            ColorChange openColor = board.set(rx, ry, rgb);
            open.add(openColor);
            queue.offer(openColor);
        }
        
        while(colorIter.hasNext()) {
            int nextColor = colorIter.next();
            
            if(isInterrupted()) {
                ThreadedPrinter.logAsync("Interrupted");
                break;
            }
            
            Iterator<ColorChange> rIter = open.iterator();
            
            ColorChange closest = null;

            long numOpen = 0;
            while(rIter.hasNext()) {
                ColorChange rNext = rIter.next();
                if(!board.isOpen(rNext.x, rNext.y)) rIter.remove();
                else if(closest == null)
                    closest = rNext;
                else if(distSqr(rNext.rgb, nextColor) < distSqr(closest.rgb, nextColor))
                    closest = rNext;
                
                numOpen++;
            }
            
            ColorChange setColor = board.setRandomNeighbor(closest.x, closest.y, nextColor);
            open.add(setColor);
            queue.offer(setColor);
            
            productionCount.incrementAndGet();
            loopSize.addAndGet(numOpen);
        }
	}
	
    private static int distSqr(int aRGB, int bRGB) {
        int dr = getR(aRGB) - getR(bRGB);
        int dg = getG(aRGB) - getG(bRGB);
        int db = getB(aRGB) - getB(bRGB);
        return dr * dr + dg * dg + db * db;
    }

    private static int getR(int rgb) { return (rgb >>> 16) & 0xFF; }
    private static int getG(int rgb) { return (rgb >>> 8) & 0xFF; }
    private static int getB(int rgb) { return rgb & 0xFF; }
	
	
}