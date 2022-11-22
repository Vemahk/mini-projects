package me.vem.art.graphics.preview;

import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.event.MouseInputAdapter;

public class PreviewMouseAdapter extends MouseInputAdapter {

    private final Preview preview;
    
    private int scaleDivOld = 1;
    
    public PreviewMouseAdapter(Preview preview) {
        this.preview = preview;
    }
    
    private Point mouseRelPos;
    @Override public void mousePressed(MouseEvent e) {
        mouseRelPos = e.getPoint();
    }
    
    @Override public void mouseDragged(MouseEvent e) {
        if(mouseRelPos == null)
            mouseRelPos = e.getPoint();
        
        preview.getPos().translate(e.getX() - mouseRelPos.x, e.getY() - mouseRelPos.y);
        mouseRelPos = e.getPoint();
    }

    private long last = 0;
    @Override public void mouseReleased(MouseEvent e) {
        mouseRelPos = null;
        if(e.getButton() == MouseEvent.BUTTON1) {
            long cur = System.currentTimeMillis();
            if(cur - last < 250) { //Double click
                scaleDivOld = preview.setScaleDiv(scaleDivOld);
                
                double mult = (double)scaleDivOld / preview.getScaleDiv();
                
                Point curPos = preview.getPos();
                Point click = e.getPoint();
                
                double x = (click.x * (1-mult) - curPos.x * mult);
                double y = (click.y * (1-mult) - curPos.y * mult);
                curPos.setLocation(x, y);
            }
            last = cur;
        }
    }
}
