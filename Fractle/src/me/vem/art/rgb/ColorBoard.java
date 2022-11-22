package me.vem.art.rgb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.vem.art.async.ThreadedPrinter;

public class ColorBoard {

    private static final byte CLOSED = -1;
    
    private final int width, height;
    
    private final int[][] rgbBoard;
    private final byte[][] neighborBoard;
    private final boolean[][] setBoard;
    
    public ColorBoard(int width, int height) {
        this.width = width;
        this.height = height;
        
        rgbBoard = new int[width][height];
        neighborBoard = new byte[width][height];
        setBoard = new boolean[width][height];
    }
    
    public ColorChange setRandomNeighbor(int x, int y, int rgb) {
        List<Dir> dirs = new ArrayList<Dir>(Dir.vals.length);

        for(Dir dir : Dir.vals) {
            int nx = x + dir.dx;
            int ny = y + dir.dy;
            
            if(!isOutOfBounds(nx, ny) && !setBoard[nx][ny]) {
                dirs.add(dir);
            }
        }
        
        if(dirs.isEmpty()) {
            ThreadedPrinter.logAsyncf("(%d,%d) No open neighbors (0x%x) ", x, y, neighborBoard[x][y]);
        }
        
        Collections.sort(dirs, (a,b) -> {
            int openAtA = 0;
            int openAtB = 0;
            for(Dir dir : Dir.vals) {
                int nax = x + a.dx + dir.dx;
                int nay = y + a.dy + dir.dy;
                if(isOutOfBounds(nax, nay) || setBoard[nax][nay])
                    openAtA++;
                
                int nbx = x + b.dx + dir.dx;
                int nby = y + b.dy + dir.dy;
                if(isOutOfBounds(nbx, nby) || setBoard[nbx][nby])
                    openAtB++;
            }
            
            return -Integer.compare(openAtA, openAtB);
        });

        int i=0;
        for(;i < dirs.size()-1 && Math.random() > .8;i++); //Favor the lower indexes heavily.
        
        Dir chosenDir = dirs.get(i);
        return set(x + chosenDir.dx, y + chosenDir.dy, rgb);
    }
    
    public ColorChange set(int x, int y, int rgb) {
        rgbBoard[x][y] = rgb;
        setBoard[x][y] = true;
        
        for(Dir dir : Dir.vals) {
            int nx = dir.dx + x;
            int ny = dir.dy + y;
            
            if(isOutOfBounds(nx, ny))
                setNeighbor(x, y, dir);
            else 
                setNeighbor(nx, ny, dir.opp());
        }
        
        return new ColorChange(x,y,rgb);
    }
    
    public boolean isOpen(int x, int y) {
        if(isOutOfBounds(x,y))
            return false;
        
        return neighborBoard[x][y] != CLOSED;
    }
    
    private void setNeighbor(int x, int y, Dir dir) {
        neighborBoard[x][y] |= 1 << dir.bn;
    }
    
    private boolean isOutOfBounds(int x, int y) {
        return x < 0 || x >= width || y < 0 || y >= height;
    }
}
