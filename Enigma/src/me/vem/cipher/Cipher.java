package me.vem.cipher;
import java.nio.ByteBuffer;

public interface Cipher {
	
	void setup(byte[] key);
	void translate(byte[] b, int len, boolean encrypt);
	
	default void setup(long l) {
		byte[] arr = new byte[8];
		ByteBuffer buf = ByteBuffer.wrap(arr);
		buf.putLong(l);
		setup(arr);
	}
	
	default void setup(int i) {
		byte[] arr = new byte[4];
		ByteBuffer buf = ByteBuffer.wrap(arr);
		buf.putInt(i);
		setup(arr);
	}
	
	default byte translate(byte b, boolean encrypt) {
		byte[] tmp = new byte[1];
		tmp[0] = b;
		translate(tmp, 1, encrypt);
		return tmp[0];
	}
}
