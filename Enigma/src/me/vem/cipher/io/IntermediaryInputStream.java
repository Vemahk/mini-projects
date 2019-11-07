package me.vem.cipher.io;

import java.io.IOException;
import java.io.InputStream;

public abstract class IntermediaryInputStream extends InputStream{

    private InputStream input;
    
    protected IntermediaryInputStream(InputStream input) {
        this.input = input;
    }

    @Override
    public int read() throws IOException {
        int i;
        
        if((i = input.read()) < 0)
            return -1;
        
        return Byte.toUnsignedInt(handle((byte)i));
    }
    
    public abstract byte handle(byte b);
}
