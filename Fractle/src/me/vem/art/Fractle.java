package me.vem.art;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import javax.imageio.ImageIO;

import me.vem.art.graphics.Preview;

public class Fractle {
	public static int WIDTH = 1;
	public static int HEIGHT = 1;
	
	//Directory where the program will save the image once done.
	public static final File SAVE = new File("saves\\");
	
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
				System.out.printf("Available commands:%n%s%n%s%n%s%n%s%n", "-repeat", "-bits [4-8]", "-save", "-numstart [1-127]");
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
		
		WIDTH <<= (colorBits*3+1)/2;
		HEIGHT <<= colorBits*3 / 2;
		
		System.out.printf("WIDTH %d | HEIGHT %d%n", WIDTH, HEIGHT);

		RGB.buildRGBLookup(colorBits);

		//Image that will be drawn to.
		image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		
		//Builds the JFrame to display the preview of the image's construction.
		Preview.build();
		
		System.out.println("Frame built.");
		
		do {
			//Builds the matrix to store the set of Pos objects that will handle which pixels are open.
			RGB.build(WIDTH, HEIGHT);
			RGB.resetAll();
			System.out.println("\nBitmap created");
			
			//The set of all open pixels. A pixel is defined as open if it is set and has a nearby unset pixel.
			LinkedList<RGB> open = new LinkedList<>();
			
			//Places the first pixel randomly from which the rest of the image builds.
			for(int x=0;x<NUM_START;x++)
				open.add(RGB.getNext().setPos(rand.nextInt(WIDTH), rand.nextInt(HEIGHT)));
			//Iteration time!
			for(RGB next = RGB.getNext(); next != null; next = RGB.getNext()) {
				if(!alive) {
					System.out.println("Interrupted");
					break;
				}
				
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
				if(save) 
					saveImage(image);
			} catch (IOException e) { e.printStackTrace(); }
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
		
		System.out.println("Image written");
	}
	
}