package me.vem.art.graphics.preview;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class PreviewWindowAdapter extends WindowAdapter {

    private final Preview preview;
    
    public PreviewWindowAdapter(Preview preview) {
        this.preview = preview;
    }

    @Override
    public void windowClosing(WindowEvent e) {
        preview.close();
    }
    
}
