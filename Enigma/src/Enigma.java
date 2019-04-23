import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;
import java.util.Stack;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * It's a nibble of an enigma, ain't it?
 * Heh.
 * I think I'm funny.
 * 
 * This program accepts a file (or directory of files) and runs a nibble (hex I suppose) level encryption
 * on it using the idea of the Enigma Machine used by Germany in WWII.
 * 
 * @author Samuel
 */
public class Enigma {
	
	public static void main(String[] args) throws IOException {
		int code = JOptionPane.showOptionDialog(null, null, "Enigma 1.3", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"Encode", "Decode"}, "Encode");
		if(code == JOptionPane.CLOSED_OPTION)
			return;
		
		JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(new File(System.getProperty("user.home") + "/Desktop/"));
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		
		File f = null;
		if(fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
			f = fc.getSelectedFile();
		
		if(f == null || !f.exists())
			return;
		
		if(code==0) encode(f);
		else if(code==1) decode(f);
	}
	
	/**
	 * Runs a hex level encryption on the given file, or if the given file object represents a directory, then it recursively runs the encryption on all sub-files. <br>
	 * Note this method will not encrypt the file if it sees that the file is already encrypted. The way it tells is if the file extension is ".lck".
	 * @param f The given file or directory to encrypt.
	 * @throws IOException If there are any problems in regards to file io. Could be either that the file doesn't exist or there was a read/write error.
	 */
	public static void encode(File f) throws IOException {

		if(f.isDirectory()) {
			for(File x : f.listFiles())
				encode(x);
			return;
		}
		
		//Do not re-encode an encoded file. Not that it isn't possible, just that it is without the design of this program to do so.
		if(f.getName().endsWith(".lck"))
			return;
		
		RandomAccessFile raf = new RandomAccessFile(f, "rw");
		
		long seed = new Random().nextLong() >>> 16;
		Machine machine = new Machine(seed);
		
		byte[] buffer = new byte[4096];
		int len = 0;
		while((len = raf.read(buffer)) > 0) {
			machine.encode(buffer, len);
			
			raf.seek(raf.getFilePointer() - len);
			raf.write(buffer, 0, len);
		}
		
		//Write the seed to the end of the file.
		raf.writeLong(seed);
		raf.close();
		
		f.renameTo(new File(f.getParentFile(), f.getName() + ".lck"));
	}
	
	/**
	 * Decodes an encrypted file, or sub-files of a directory, that were encrypted using the above function. <br>
	 * Note that the file will be decoded if this method does not recognize it as encrypted. The way this method tells is if the extension is ".lck".
	 * @param f The file to decode.
	 * @throws IOException If there is any io problem. This could be that the file does not exist, or there was a problem while read/writing.
	 */
	public static void decode(File f) throws IOException {
		
		if(f.isDirectory()) {
			for(File x : f.listFiles())
				decode(x);
			return;
		}
		
		//Do not decode a file that isn't locked.
		//This is the one of the reasons we aren't encoding encoded files.
		//The program removes the .lck when it's done decoding.
		if(!f.getName().endsWith(".lck"))
			return; 
		
		RandomAccessFile raf = new RandomAccessFile(f, "rw");
		
		//Read the seed from the end of the file.
		raf.seek(raf.length() - 8);
		long seed = raf.readLong();
		
		raf.setLength(raf.length() - 8);
		raf.seek(0);
		
		Machine machine = new Machine(seed);
		
		byte[] buf = new byte[4096];
		int len = 0;
		while((len = raf.read(buf)) > 0) {
			machine.encode(buf, len);
			
			raf.seek(raf.getFilePointer() - len);
			raf.write(buf, 0, len);
		}
		
		raf.close();
		
		//Rename the file to exclude the .lck extension.
		String fName = f.getName();
		fName = fName.substring(0, fName.lastIndexOf('.'));
		f.renameTo(new File(f.getParentFile(), fName));
	}
}

/**
 * This is the Enigma Machine object in-concept. Its subclass, Rotor, is supposed to represent the rotors
 * within the machine. They take inputs of bytes and using 'connections' change the bytes around depending
 * on their settings and whether the 'signal' is passing from left to right or right to left. 
 * 
 * @author Samuel
 */
class Machine{
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
			else {
				byte r2 = stack.pop();
				out[r] = r2;
				out[r2] = r;
			}
		}
		
		return out;
	}
	
	/**
	 * Swaps the contents of indexes a and b within the array arr.
	 * @param arr
	 * @param a
	 * @param b
	 */
	public static void swap(byte[] arr, int a, int b) {
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
	private static class Rotor{
		
		private byte[] ltr, rtl;
		private int size, offset;
		
		public Rotor(byte... in) { this(0, in); }
		
		public Rotor(int initOffset, byte... in) {
			if(in.length != 16)
				throw new IllegalArgumentException("Rotors in this instance should be of size 16.");
			size = in.length;
			ltr = new byte[size];
			rtl = new byte[size];
			for(byte i=0;i<size;i++) {
				ltr[i] = in[i];
				rtl[in[i]] = i;
			}
			
			rotate(initOffset);
		}
		
		/**
		 * Rotates this rotor 't' times.
		 * @param t The number of turns to rotate this rotor.
		 * @return The number of times it passed 0. (i.e. the number of times the next rotor in the sequence should be rotated)
		 */
		public long rotate(long t) {int ori = offset;
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

			i = (byte) ((ltr[i] + offset)%size);
			return i;
		}
		
		/**
		 * Translates a hex digit that is passing from the 'right' to the 'left' side of this rotor.
		 * @param i The hex digit to be translated.
		 * @return The encoded hex digit.
		 */
		public byte rtl(byte i) {
			if((i -= offset) < 0)
				i+=size;

			i = (byte) ((rtl[i] + offset)%size);
			return i;
		}
		
		/**
		 * Used mostly for debug in this program. Is not used within this implementation of the program.
		 */
		public String toString() {
			return String.format("%s%n%s%nOffset: %d", new String(ltr), new String(rtl), offset);
		}
	}
}