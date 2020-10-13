import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.util.Arrays;

import javax.swing.JFrame;

public class ParetoCanvas extends Canvas{

    private static final long serialVersionUID = 1084140395261829797L;

    private final BufferStrategy bufferStrategy;
    private final int[] data;
    private final int size;
    
    public ParetoCanvas(JFrame parentFrame, int[] data) {

        this.data = data;
        this.size = data.length;
        
        this.setBackground(Color.BLACK);
        this.setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());
        
        this.addKeyListener(new KeyListener() {
            @Override public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    parentFrame.dispose();
                    System.exit(0);
                }
                if(e.getKeyCode() == KeyEvent.VK_SPACE)
                    Pareto.PAUSE = !Pareto.PAUSE;
                if(e.getKeyCode() == KeyEvent.VK_UP)
                    Pareto.UPMS++;
                if(e.getKeyCode() == KeyEvent.VK_DOWN && Pareto.UPMS > 0)
                    Pareto.UPMS--;
            }   
            
            @Override public void keyReleased(KeyEvent e) {}
            @Override public void keyTyped(KeyEvent e) {}
        });
        
        parentFrame.add(this);
        parentFrame.setResizable(false);
        parentFrame.pack();
        parentFrame.setLocationRelativeTo(null);
        
        this.setIgnoreRepaint(true);
        this.createBufferStrategy(2);
        this.bufferStrategy = this.getBufferStrategy();
        
        parentFrame.setVisible(true);
    }
    
    /**
     * Anti-flicker
     */
    public void render() {
        if(bufferStrategy == null) return;
        
        Graphics g = null;
        try {
            g = bufferStrategy.getDrawGraphics();
            draw(g);
        } finally {
            if(g != null)
                g.dispose();
        }
        
        bufferStrategy.show();
    }
    
    public void draw(Graphics g) {
        g.setColor(this.getBackground());
        g.fillRect(0, 0, this.getWidth(), this.getHeight());

        g.setColor(Color.WHITE);
        g.drawString("UPMS: " + Pareto.UPMS, 5, 15);
        synchronized(data) {
            Arrays.sort(data);
            
            //Top section
            double max = data[size-1];
            for(int i=0;i<size;i++)
                g.drawLine(i, this.getHeight()/2-1, i, this.getHeight()/2 - 1 - (int)(data[i] / max * this.getHeight()/2));
            
            //Bottom section
            int modeval = 0;
            int modec = 0;
            
            int range = data[size-1] - data[0] + 1;
            
            int[] count = new int[range];
            for(int i=0, c=0;i < size;i++) {
                if(i!=0 && data[i] != data[i-1])
                    c++;
                count[c]++;
                if(modec == c) {
                    modeval++;
                }else if(modec != c && count[c] > modeval) {
                    modec = c;
                    modeval = count[c];
                }
            }
            
            int heightrange = this.getHeight()/2 - 5;
            for(int i=0;i<range;i++) {
                
                g.fillRect(i * this.getWidth() / range, this.getHeight()/2 + 5 + (int)((1-((double)count[i] / modeval)) * heightrange) , this.getWidth() / range, this.getHeight()/2);
            }
        }
    }
}
