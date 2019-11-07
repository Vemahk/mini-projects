package me.vem.cipher.io;

import java.io.IOException;
import java.io.OutputStream;

public abstract class IntermediaryOutputStream extends OutputStream {

    private OutputStream target;
    
    protected IntermediaryOutputStream(OutputStream target) {
        this.target = target;
    }
    
    @Override
    public void write(int b) throws IOException {
        target.write(handle((byte)b));
    }
    
    public abstract byte handle(byte b);
}
