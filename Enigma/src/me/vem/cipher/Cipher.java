package me.vem.cipher;

public interface Cipher {
	byte translate(byte b, boolean encrypt);
	default void translate(byte[] b, int len, boolean encrypt) {
	    for(int i=0;i<len;i++)
	        b[i] = translate(b[i], encrypt);
	}
}
