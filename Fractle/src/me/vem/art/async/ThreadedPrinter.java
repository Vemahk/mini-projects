package me.vem.art.async;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ThreadedPrinter {

    private static ThreadedPrinter instance;
    public static ThreadedPrinter getInstance() {
        if(instance == null)
            instance = new ThreadedPrinter();
        
        if(!instance.printThread.isAlive())
            instance.restart();
        
        return instance;
    }
    
    public static void logAsync(String str) {
        getInstance().enqueue(str);
    }
    
    public static void logAsyncf(String format, Object... args) {
        logAsync(String.format(format, args));
    }
    
    private Thread printThread;
    private BlockingQueue<String> queue;
    
    private ThreadedPrinter() {
        queue = new LinkedBlockingQueue<String>();
        
        restart();
    }
    
    public void enqueue(String str) {
        queue.offer(str);
    }
    
    public void restart() {
        if(printThread != null && printThread.isAlive()) {
            log("Restarting ThreadedPrinter...");
            printThread.interrupt();
        }   
        
        printThread = new Thread(() -> {
            while(true) {
                try {
                    log(queue.take());
                } catch (InterruptedException e) {
                    log("ThreadedPrinter interrupted...");
                    return;
                }
            }
        });
        
        printThread.setDaemon(true);
        printThread.start();
    }
    
    private static SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static void log(String str) {
        System.out.printf("[%s] %s%n", sdfDate.format(new Date()), str);
    }
}
