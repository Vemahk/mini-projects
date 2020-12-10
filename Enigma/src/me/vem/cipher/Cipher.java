package me.vem.cipher;
import java.nio.ByteBuffer;

public interface Cipher {
	
    Cipher setup(byte[] key);
	byte translate(byte b, boolean encrypt);
	
	default void translate(byte[] b, int len, boolean encrypt) {
	    for(int i=0;i<len;i++)
	        b[i] = translate(b[i], encrypt);
	}
	
	default Cipher setup(long l) {
		byte[] arr = new byte[8];
		ByteBuffer buf = ByteBuffer.wrap(arr);
		buf.putLong(l);
		return setup(arr);
	}
	
	default Cipher setup(int i) {
		byte[] arr = new byte[4];
		ByteBuffer buf = ByteBuffer.wrap(arr);
		buf.putInt(i);
		return setup(arr);
	}
}
