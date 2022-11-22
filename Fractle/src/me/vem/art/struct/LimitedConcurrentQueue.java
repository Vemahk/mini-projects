package me.vem.art.struct;

import java.util.concurrent.ConcurrentLinkedQueue;

public class LimitedConcurrentQueue<T> extends ConcurrentLinkedQueue<T>{

    private static final long serialVersionUID = -5802723028977992524L;
    
    private boolean isProductionOver = false;
    
    public void endProduction() {
        isProductionOver = true;
    }
    
    public boolean hasNext() throws InterruptedException {
        boolean wasProductionOver = isProductionOver;
        
        while(isEmpty()) {
            if(wasProductionOver)
                return false;
            else {
                Thread.sleep(50);
                wasProductionOver = isProductionOver;
            }
        }
        
        return true;
    }
}
