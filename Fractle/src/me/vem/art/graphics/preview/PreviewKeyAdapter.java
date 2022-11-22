package me.vem.art.graphics.preview;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class PreviewKeyAdapter extends KeyAdapter {

    private final Preview preview;
    
    public PreviewKeyAdapter(Preview preview) {
        this.preview = preview;
    }

    @Override public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ESCAPE) 
            preview.close();
    }
}
