package me.vem.cipher.io;

import java.io.InputStream;

import me.vem.cipher.Cipher;

public class CipheredInputStream extends IntermediaryInputStream {

    private Cipher cipher;
    
    protected CipheredInputStream(InputStream input, Cipher cipher) {
        super(input);
        this.cipher = cipher;
    }

    @Override
    public byte handle(byte b) {
        return cipher.translate(b, false);
    }
}
