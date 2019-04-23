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
	
	public static void encode(File f) throws IOException {

		if(f.isDirectory()) {
			for(File x : f.listFiles())
				encode(x);
			return;
		}
		
		//Do not re-encode an encoded file.
		//But I mean, we could tho. Just... Don't.
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
		
		raf.writeLong(seed);
		raf.close();
		
		f.renameTo(new File(f.getParentFile(), f.getName() + ".lck"));
	}
	
	public static void decode(File f) throws IOException {
		
		if(f.isDirectory()) {
			for(File x : f.listFiles())
				decode(x);
			return;
		}
		
		//Do not decode a file that isn't locked.
		//This is the reason we aren't encoding encoded files.
		//The program removes the .lck when it's done decoding.
		if(!f.getName().endsWith(".lck"))
			return; 
		
		RandomAccessFile raf = new RandomAccessFile(f, "rw");
		raf.seek(raf.length() - 8);
		long seed = raf.readLong();
		
		raf.setLength(raf.length() - 8);
		raf.seek(0);
		
		Machine machine = new Machine(seed);
		
		//Let's just hope the size of the file is less than Integer.MAX_VALUE;
		byte[] buf = new byte[4096];
		int len = 0;
		while((len = raf.read(buf)) > 0) {
			machine.encode(buf, len);
			
			raf.seek(raf.getFilePointer() - len);
			raf.write(buf, 0, len);
		}
		
		raf.close();
		
		String fName = f.getName();
		fName = fName.substring(0, fName.lastIndexOf('.'));
		f.renameTo(new File(f.getParentFile(), fName));
	}
}

class Machine{
	private Rotor[] rotors;
	private Rotor reflect;
	private Random rand;
	
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
	
	public byte encode(byte b) {
		return (byte) (translate((byte) ((b & 0xF0) >>> 4)) << 4 | translate((byte) (b & 0xF)));
	}
	
	public void encode(byte[] in, int len) {
		for(int i=0;i<in.length && i < len;i++)
			in[i] = encode(in[i]);
	}
	
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
	
	public void adjust(long by) {
		//This is the trashiest kind of for-loop
		//Don't be like me.
		for(int i=0; i < rotors.length && (by = rotors[i++].rotate(by)) > 0;);
	}
	
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
	
	public static void swap(byte[] arr, int a, int b) {
		byte c = (byte) (arr[a] ^ arr[b]);
		arr[a] ^= c;
		arr[b] ^= c;
	}
	
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
		 * @param t
		 * @return The number of times it passed 0. (i.e. the number of times the next rotor in the sequence should be rotated)
		 */
		public long rotate(long t) {int ori = offset;
			offset = (int) ((offset + t) % size);
			return t/size + (offset < ori ? 1 : 0);
		}
		
		public byte ltr(byte i) {
			if((i -= offset) < 0)
				i+=size;

			i = (byte) ((ltr[i] + offset)%size);
			return i;
		}
		
		public byte rtl(byte i) {
			if((i -= offset) < 0)
				i+=size;

			i = (byte) ((rtl[i] + offset)%size);
			return i;
		}
		
		public String toString() {
			return String.format("%s%n%s%nOffset: %d", new String(ltr), new String(rtl), offset);
		}
	}
}

