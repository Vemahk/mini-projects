package me.vem.art;

import java.util.function.Supplier;

import me.vem.art.async.ThreadedPrinter;
import me.vem.art.graphics.preview.Preview;
import me.vem.art.rgb.ColorChange;
import me.vem.art.rgb.ColorWheel;

public class Program {

    private static boolean save = false;
    private static boolean repeat = false;
    private static byte colorBits = 6;
    private static byte numStart = 1;
    private static int scaleDiv = 1;
    
    private static int width;
    private static int height;
    
    private static boolean help = false;
    
    public static void main(String... args) throws InterruptedException {
        parseArgs(args);
        if(isHelp())
            return;

        ThreadedPrinter.logAsyncf("WIDTH %d | HEIGHT %d%n", width, height);
        
        //Builds the JFrame to display the preview of the image's construction.
        Preview preview = new Preview(width, height, scaleDiv);
        preview.start();
        
        ColorWheel colorWheel = setupTask(() -> new ColorWheel(colorBits), "ColorWheel");
        Fractle fractle = new Fractle(colorWheel, width, height, numStart, repeat);
        fractle.start();

        while(fractle.queue.hasNext()) {
            ColorChange diff = fractle.queue.poll();
            preview.setRGB(diff.x, diff.y, diff.rgb);
        }
        
        if(save) preview.save();
        preview.interrupt();
        preview.render();
    }
    
    private static void parseArgs(String... args) {
        for(int i=0;i<args.length;i++) {
            if("-repeat".equals(args[i]))
                repeat = true;
            if("-bits".equals(args[i]) && args.length > i + 1)
                try {
                    colorBits = Byte.parseByte(args[i+1]);
                    switch(colorBits) {
                    case 8: scaleDiv <<= 1;
                    case 7: scaleDiv <<= 2;
                    default: break;
                    }
                }catch (NumberFormatException e) { e.printStackTrace(); }
            if("-save".equals(args[i]))
                save = true;
            if("-help".equals(args[i])) {
                help = true;
            }
            if("-numstart".equals(args[i]) && args.length > i + 1)
                try {
                    numStart = Byte.parseByte(args[i+1]);
                }catch(NumberFormatException e) {e.printStackTrace();}
        }
        
        width = 1 << ((colorBits*3+1)/2);
        height = 1 << (colorBits * 3 / 2);
    }
    
    private static boolean isHelp() {
        if(help)
            System.out.printf("Available commands:%n%s%n%s%n%s%n%s%n", "-repeat", "-bits [4-8]", "-save", "-numstart [1-127]");
        
        return help;
    }
    
    private static <T> T setupTask(Supplier<T> task, String name) {
        long start = System.nanoTime();
        T t = task.get();
        long end = System.nanoTime();
        
        ThreadedPrinter.logAsyncf("Setup Task: %s, Finished in %.3f seconds.", name, (end - start) / 1_000_000_000.0);
        return t;
    }
}
