package me.vem.art.struc;

import java.util.Iterator;

import me.vem.art.Pixel;

public class OpenPixelList {

    private static OpenPixelList instance;
    public static OpenPixelList getInstance() {
        if(instance == null)
            instance = new OpenPixelList();
        return instance;
    }
    
    private OpenPixelNode head, tail;
    private int size;
    
    private OpenPixelList() {}
    
    public Iterator<Pixel> iterator() {
        return new Iterator<Pixel>() {
            private OpenPixelNode next = head;
            
            @Override
            public boolean hasNext() {
                return next != null;
            }

            @Override
            public Pixel next() {
                Pixel pixel = next.getPixel();
                next = next.getNext();
                return pixel;
            }
        };
    }
    
    public synchronized boolean add(Pixel pixel) {
        if(contains(pixel))
            return false;
        
        OpenPixelNode node = new OpenPixelNode(pixel);
        pixel.setListNode(node);
        
        if(isEmpty())
            head = tail = node;
        else {
            tail.setNext(node);
            node.setPrev(tail);
            tail = node;
        }
        
        size++;
        return true;
    }
    
    public synchronized boolean remove(Pixel pixel) {
        OpenPixelNode node = pixel.getNode();
        if(node == null) return false;
        pixel.setListNode(null);
        
        OpenPixelNode next = node.getNext(), prev = node.getPrev();
        
        if(next != null)
            next.setPrev(prev);
        
        if(prev != null)
            prev.setNext(next);
        
        if(node == head)
            head = next;
        
        if(node == tail)
            tail = prev;
        
        size--;
        return true;
    }
    
    public synchronized boolean contains(Pixel pixel) {
        return pixel.getNode() != null;
    }
    
    public synchronized void clear() {
        head = tail = null;
    }
    
    public int size() {
        return size;
    }
    
    public boolean isEmpty() {
        return head == null;
    }
    
    public static class OpenPixelNode{
        private final Pixel pixel;
        
        private OpenPixelNode prev, next;
        
        public OpenPixelNode(Pixel pixel) {
            this.pixel = pixel;
        }
        
        public Pixel getPixel() {return pixel;}
        public OpenPixelNode getPrev() {return prev;}
        public OpenPixelNode getNext() {return next;}
        
        public void setPrev(OpenPixelNode node) {prev = node;}
        public void setNext(OpenPixelNode node) {next = node;}
    }
}

