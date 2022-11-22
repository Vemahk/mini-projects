package me.vem.art;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import javax.imageio.ImageIO;

import me.vem.art.async.ThreadedPrinter;
import me.vem.art.graphics.preview.Preview;
import me.vem.art.rgb.ColorWheel;
import me.vem.art.rgb.RGB;

public class Fractle {
	//Directory where the program will save the image once done.
	public static final File SAVE = new File("saves\\");
	
	private static ColorWheel colorWheel;
	private static BufferedImage image;
	public static boolean alive = true;
	
	public static Random rand = new Random();
	
	public static void main(String... args) throws IOException, InterruptedException {
		
	    FractleArgs fractleArgs = new FractleArgs(args);
	    if(fractleArgs.isHelp())
	        return;
	    
		int WIDTH = fractleArgs.getWidth();
		int HEIGHT = fractleArgs.getHeight();
		
		System.out.printf("WIDTH %d | HEIGHT %d%n", WIDTH, HEIGHT);
		
		setupTask(() -> {
		    colorWheel = new ColorWheel(fractleArgs.colorBits);
		}, "Lookup");
		
		setupTask(() -> {
	        //Image that will be drawn to.
	        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB); 
        }, "Image");
        
        setupTask(() -> {
            //Builds the JFrame to display the preview of the image's construction.
            new Preview(image, fractleArgs.scaleDiv);
        }, "Preview");
		
		while(alive) {
			//Builds the matrix to store the set of Pos objects that will handle which pixels are open.
			RGB.build(WIDTH, HEIGHT);
			colorWheel.reset();
			
			ThreadedPrinter.logAsync("Bitmap created");
			
			//The set of all open pixels. A pixel is defined as open if it is set and has a nearby unset pixel.
			LinkedList<RGB> open = new LinkedList<>();
			
			//Places the first pixel randomly from which the rest of the image builds.
			for(int x=0;x<fractleArgs.NUM_START;x++) {
			    open.add(colorWheel.next().setPos(rand.nextInt(WIDTH), rand.nextInt(HEIGHT), image));
			}
			
			//Iteration time!
			int count = 0;
			long milli = System.currentTimeMillis();
			
			while(colorWheel.hasNext()) {
			    RGB next = colorWheel.next();
			    
				if(!alive) {
					ThreadedPrinter.logAsync("Interrupted");
					break;
				}
				
				Iterator<RGB> rIter = open.iterator();
				
				RGB closest = null;
				
				while(rIter.hasNext()) {
					RGB rNext = rIter.next();
					if(!rNext.isOpen()) rIter.remove();
					else if(closest == null)
						closest = rNext;
					else if(RGB.distSqr(rNext, next) < RGB.distSqr(closest, next))
						closest = rNext;
				}
				closest.setRandomly(next, image);
				
				if(next.isOpen())
					open.add(next);
				
				count++;
				
				long currTime = System.currentTimeMillis();
				if(currTime - milli >= 1000) {
				    ThreadedPrinter.logAsyncf("Placed %d pixels. Current open size: %d. Weight: %d", count, open.size(), count * open.size());
				    
			        milli = currTime;
                    count = 0;
				}
			}
			
			ThreadedPrinter.logAsync("Image built");
			
			try {
				if(fractleArgs.save) 
					saveImage(image);
			} catch (IOException e) { e.printStackTrace(); }
			
			if(!fractleArgs.repeat)
			    break;
		}
	}
	
	private static void setupTask(Runnable task, String name) {
	    long startTime = System.currentTimeMillis();
	    task.run();
	    long endTime = System.currentTimeMillis();
	    
	    ThreadedPrinter.logAsyncf("Setup Task: %s, Finished in %.3f seconds.", name, (endTime - startTime) / 1000f);
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
		ThreadedPrinter.logAsync("Out file: "+outFile.getName());
		ImageIO.write(image, "png", outFile);
		
		ThreadedPrinter.logAsync("Image written");
	}
	
}