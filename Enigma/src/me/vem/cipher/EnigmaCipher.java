package me.vem.cipher;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.Stack;

public class EnigmaCipher implements Cipher{

	private Machine machine;
	
	public EnigmaCipher(byte[] key) {
	    init(key);
	}
	
	public EnigmaCipher(long l) {
        byte[] arr = new byte[8];
        ByteBuffer buf = ByteBuffer.wrap(arr);
        buf.putLong(l);
        init(arr);
	}
    
    public EnigmaCipher(int i) {
        byte[] arr = new byte[4];
        ByteBuffer buf = ByteBuffer.wrap(arr);
        buf.putInt(i);
        init(arr);
    }
	
	private void init(byte[] key) {
        if(key.length < 8) {
            byte[] tmp = new byte[8];
            for(int i=0;i<key.length;i++)
                tmp[i] = key[i];
            key = tmp;
        }
        
        ByteBuffer buf = ByteBuffer.wrap(key);
        long l = buf.getLong();
        
        machine = new Machine(l);
	}

	@Override
	public byte translate(byte b, boolean encrypt) {
	    return machine.encode(b);
	}
	
	public void translate(byte[] b, int len) {
		machine.encode(b, len);
	}

	/**
	 * This is the Enigma Machine object in-concept. Its subclass, Rotor, is supposed to represent the rotors
	 * within the machine. They take inputs of bytes and using 'connections' change the bytes around depending
	 * on their settings and whether the 'signal' is passing from left to right or right to left. 
	 * 
	 * @author Samuel
	 */
	private class Machine{
		private Rotor[] rotors; //The rotors of the machine.
		private Rotor reflect; //The reflection rotor.
		private Random rand; //The random object used for creation.
		
		public Machine(long seed) {
			this(new Random(seed), 3);
		}

		public Machine(Random rand, int size) {
			this.rand = rand;
			
			rotors = new Rotor[size];
			for(int i=0;i<size;i++)
				rotors[i] = new Rotor(rand.nextInt(16), shuffle());
			
			reflect = new Rotor(getRefl());
		}
		
		/**
		 * Encodes a single byte, b, be independently encoding the two hex digits that represent it.
		 * @param b The byte to be encoded.
		 * @return The encoded byte.
		 */
		public byte encode(byte b) {
			return (byte) (translate((byte) ((b & 0xF0) >>> 4)) << 4 | translate((byte) (b & 0xF)));
		}
		
		/**
		 * Encode a set of bytes, starting at index 0 of the array and going to len-1.
		 * @param in The array of bytes to encode.
		 * @param len The number of bytes within the array to encode.
		 */
		public void encode(byte[] in, int len) {
			for(int i=0;i<in.length && i < len;i++)
				in[i] = encode(in[i]);
		}
		
		/**
		 * This is the method that encodes a specific hex digits. It passes it through each of the rotors, 
		 * left to right, then through the reflection rotor, and then back through the rotors right-to-left. 
		 * It then rotates as many rotors as is warranted by the current state of the machine.
		 * @param b
		 * @return
		 */
		public byte translate(byte b) {
			int i=0;
			while(i < rotors.length)
				b = rotors[i++].ltr(b);
			
			b = reflect.ltr(b);
			
			while(i > 0)
				b = rotors[--i].rtl(b);
			
			adjust(1);
			return b;
		}
		
		/**
		 * Rotates the machine 'by' times.
		 * @param by
		 */
		public void adjust(long by) {
			//This may be the most beautiful for-loop I have ever written in my life.
			for(int i=0; i < rotors.length && (by = rotors[i++].rotate(by)) > 0;);
		}
		
		/**
		 * @return A byte array with contents 0 through 15, inclusive, in a random order.
		 */
		public byte[] shuffle() {
			byte[] out = new byte[16];
			for(byte i=1;i<16;i++)
				out[i] = i;
			
			for(int i=0;i<out.length;i++) {
				int n = i + rand.nextInt(out.length - i);
				swap(out, i, n);
			}
			return out;
		}
		
		/**
		 * @return A byte array made specifically for the reflection rotor. Because of the nature of the
		 * reflection rotor, this could not be random like the other rotors.
		 */
		public byte[] getRefl() {
			Stack<Byte> stack = new Stack<>();
			for(byte i=0;i<16;i++)
				stack.add(rand.nextInt(i+1), i);
			
			byte[] out = new byte[16];
			while(!stack.isEmpty()) {
				byte r = stack.pop();
				if(stack.isEmpty())
					out[r] = r;
				else out[out[r] = stack.pop()] = r; //Also not originally written like this, but refactoring is fun.
			}
			
			return out;
		}
		
		/**
		 * Swaps the contents of indexes a and b within the array arr.
		 * @param arr
		 * @param a
		 * @param b
		 */
		public void swap(byte[] arr, int a, int b) {
			byte c = (byte) (arr[a] ^ arr[b]);
			arr[a] ^= c;
			arr[b] ^= c;
		}
		
		/**
		 * The rotor class which is used by the Machine class. For the purposes of this program, it is 
		 * forced to only accept an array of 16 bytes, which it presumes contains contents 0 to 15 inclusive. 
		 * It then initializes its internal arrays and offset to get ready for processing incoming hex digits. 
		 * @author Samuel
		 *
		 */
		private class Rotor{
			
			private byte[] ltr, rtl;
			private int size, offset;
			
			public Rotor(byte... in) { this(0, in); }
			
			public Rotor(int initOffset, byte... in) {
				if(in.length != 16)
					throw new IllegalArgumentException("Rotors in this instance should be of size 16.");
				size = in.length;
				ltr = new byte[size];
				rtl = new byte[size];
				
				for(byte i=0;i<size;i++) 
					rtl[ltr[i] = in[i]] = i; //Mind you, this was not originally written like this. I just changed it to this because I thought it'd look cool. I wasn't disappointed.
				
				rotate(initOffset);
			}
			
			/**
			 * Rotates this rotor 't' times.
			 * @param t The number of turns to rotate this rotor.
			 * @return The number of times it passed 0. (i.e. the number of times the next rotor in the sequence should be rotated)
			 */
			public long rotate(long t) {
				int ori = offset;
				offset = (int) ((offset + t) % size);
				return t/size + (offset < ori ? 1 : 0);
			}
			
			/**
			 * Translates a hex digit that is passing from the 'left' to the 'right' side of this rotor.
			 * @param i The hex digit to be translated.
			 * @return The encoded hex digit.
			 */
			public byte ltr(byte i) {
				if((i -= offset) < 0)
					i+=size;
				
				return i = (byte) ((ltr[i] + offset)%size);
			}
			
			/**
			 * Translates a hex digit that is passing from the 'right' to the 'left' side of this rotor.
			 * @param i The hex digit to be translated.
			 * @return The encoded hex digit.
			 */
			public byte rtl(byte i) {
				if((i -= offset) < 0)
					i+=size;

				return i = (byte) ((rtl[i] + offset)%size);
			}
			
			/**
			 * Used mostly for debug in this program. Is not used within this implementation of the program.
			 */
			public String toString() {
				return String.format("%s%n%s%nOffset: %d", new String(ltr), new String(rtl), offset);
			}
		}
	}
}
