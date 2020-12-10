package me.vem.art.struc;

import me.vem.art.Pixel;

public class OpenPixelList {

    private static OpenPixelList instance;
    public static OpenPixelList getInstance() {
        if(instance == null)
            instance = new OpenPixelList();
        return instance;
    }
    
    private OpenPixelNode head, tail;
    
    private OpenPixelList() {}
    
    public synchronized void add(Pixel pixel) {
        OpenPixelNode node = new OpenPixelNode(pixel);
        if(isEmpty())
            head = tail = node;
        else {
            tail.setNext(node);
            node.setPrev(tail);
            tail = node;
        }
    }
    
    public synchronized void remove(Pixel pixel) {
        OpenPixelNode node = pixel.getNode();
        if(node == null) return;
        
        OpenPixelNode next = node.getNext(), prev = node.getPrev();
        
        if(next != null)
            next.setPrev(prev);
        
        if(prev != null)
            prev.setNext(next);
        
        if(node == head)
            head = next;
        
        if(node == tail)
            tail = prev;
    }
    
    public synchronized void clear() {
        head = tail = null;
    }
    
    public boolean isEmpty() {
        return head == null;
    }
    
    public static class OpenPixelNode{
        private final Pixel pixel;
        
        private OpenPixelNode prev, next;
        
        public OpenPixelNode(Pixel pixel) {
            this.pixel = pixel;
            pixel.setListNode(this);
        }
        
        public Pixel getPixel() {return pixel;}
        public OpenPixelNode getPrev() {return prev;}
        public OpenPixelNode getNext() {return next;}
        
        public void setPrev(OpenPixelNode node) {prev = node;}
        public void setNext(OpenPixelNode node) {next = node;}
    }
}

