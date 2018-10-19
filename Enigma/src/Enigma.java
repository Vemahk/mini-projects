import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.Stack;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class Enigma {

	public static final boolean DEBUG = false;
	
	public static Random rand;
	
	public static Rotor l;
	public static Rotor m;
	public static Rotor r;
	public static Rotor ref;
	
	public static void main(String[] args) throws IOException {
		int code = JOptionPane.showOptionDialog(null, null, "Enigma 1.1", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"Encode", "Decode"}, "Encode");
		
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
		
		if(f.getName().endsWith(".lck"))
			return; //Do not re-encode an encoded file.
		
		FileInputStream fis = new FileInputStream(f);
		
		rand = new Random();
		long seed = 0;
		rand.setSeed(seed = rand.nextLong() >>> 16);
		
		l = new Rotor(shuffle()).init(rand.nextInt(16));
		m = new Rotor(shuffle()).init(rand.nextInt(16));
		r = new Rotor(shuffle()).init(rand.nextInt(16));
		ref = new Rotor(getRefl());

		if(DEBUG)
			System.out.printf("%s%n%s%n%s%n%s%n", l, m, r, ref);
		
		File outFile = new File(f.getParentFile(), f.getName()+ ".lck");
		FileOutputStream fos = new FileOutputStream(outFile);
		
		ByteBuffer buf = ByteBuffer.allocate(8);
		buf.putLong(seed);
		fos.write(buf.array());
		
		byte[] buffer = new byte[4096];
		int len = 0;
		while((len = fis.read(buffer)) > 0) {
			encode(buffer, len);
			fos.write(buffer, 0, len);
		}
		
		fis.close();
		fos.flush();
		fos.close();

		f.delete();
	}
	
	public static void decode(File f) throws IOException {
		
		if(f.isDirectory()) {
			for(File x : f.listFiles())
				decode(x);
			return;
		}
		
		if(!f.getName().endsWith(".lck"))
			return; //Do not decode a file that isn't locked.
		
		FileInputStream fis = new FileInputStream(f);
		
		long seed = 0;
		long size = f.length() - 8;
		
		ByteBuffer bbuf = ByteBuffer.allocate(8);
		
		fis.read(bbuf.array());
		seed = bbuf.getLong();
		
		if(seed == 0) {
			System.out.println("Seed not found...");
			fis.close();
			return;
		}
		
		rand = new Random(seed);

		l = new Rotor(shuffle()).init(rand.nextInt(16));
		m = new Rotor(shuffle()).init(rand.nextInt(16));
		r = new Rotor(shuffle()).init(rand.nextInt(16));
		ref = new Rotor(getRefl());
		
		for(long i=0;i<size*2;i++)
			if(r.rotate() && m.rotate() && l.rotate());
		
		if(DEBUG) System.out.printf("%s%n%s%n%s%n%s%n", l, m, r, ref);
		
		//Let's just hope the size of the file is less than Integer.MAX_VALUE;
		byte[] buf = new byte[(int)size];
		bbuf = ByteBuffer.wrap(buf);
		fis.read(buf);
		fis.close();
		decode(buf);
		
		File outFile = new File(f.getParentFile(), f.getName().substring(0, f.getName().length()-4));
		FileOutputStream fos = new FileOutputStream(outFile);
		
		fos.write(bbuf.array());
		fos.flush();
		fos.close();
		
		f.delete();
	}
	
	public static void encode(byte[] in, int len) {
		for(int i=0;i<in.length && i < len;i++)
			in[i] = encode(in[i]);
	}
	
	public static byte encode(byte b) {
		byte left = (byte) ((b & 0xF0) >>> 4);
		byte right = (byte) (b & 0xF);
		
		left = Rotor.translate(left, l, m, r, ref, true);
		right = Rotor.translate(right, l, m, r, ref, true);
		
		if(DEBUG) System.out.println();
		
		return (byte) (left << 4 | right);
	}
	
	public static void decode(byte[] in){
		for(int i=in.length-1;i>=0;i--)
			in[i] = decode(in[i]);
	}
	
	public static byte decode(byte b) {
		byte left = (byte) ((b & 0xF0) >>> 4);
		byte right = (byte) (b & 0xF);

		right = Rotor.translate(right, l, m, r, ref, false);
		left = Rotor.translate(left, l, m, r, ref, false);
		
		if(DEBUG) System.out.println();
		return (byte)(left << 4 | right); 
	}
	
	public static byte[] shuffle() {
		byte[] out = new byte[16];
		for(byte i=1;i<16;i++)
			out[i] = i;
		
		for(int i=0;i<out.length;i++) {
			int n = i + rand.nextInt(out.length - i);
			swap(out, i, n);
		}
		return out;
	}
	
	public static byte[] getRefl() {
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
}

class Rotor{
	
	public static byte translate(byte b, Rotor l, Rotor m, Rotor r, Rotor ref, boolean forward) {
		if(!forward)
			if(r.rotateBack() && m.rotateBack() && l.rotateBack());
		
		b = l.ltr(b);
		b = m.ltr(b);
		b = r.ltr(b);
		b = ref.ltr(b);
		b = r.rtl(b);
		b = m.rtl(b);
		b = l.rtl(b);
		
		if(forward) if(r.rotate() && m.rotate() && l.rotate());
		return b;
	}
	
	private byte[] ltr;
	private byte[] rtl;
	
	private int size;
	private int offset;
	
	public Rotor(byte... in) {
		if(in.length != 16)
			System.out.println("Ya goofed.");
		size = in.length;
		ltr = new byte[size];
		rtl = new byte[size];
		for(byte i=0;i<size;i++) {
			ltr[i] = in[i];
			rtl[in[i]] = i;
		}
	}
	
	public Rotor init(int off) {
		for(int i=0;i<off;i++)
			rotate();
		return this;
	}
	
	public int getOffset() { return offset; }
	
	public boolean rotate() {
		if(Enigma.DEBUG) System.out.printf("Offset: %d -> %d%n", offset, (offset+1)%size);
		offset = ++offset % size;
		return offset == 0;
	}
	
	public boolean rotateBack() {
		if(--offset < 0) offset+=size;
		if(Enigma.DEBUG) System.out.printf("Offset: %d -> %d%n", (offset+1)%size, offset);
		return offset == size-1;
	}
	
	public byte ltr(byte i) {
		byte ori = i;
		if((i -= offset) < 0)
			i+=size;

		i = (byte) ((ltr[i] + offset)%size);
		if(Enigma.DEBUG)
			System.out.printf("%d mapped to %d%n", ori, i);
		return i;
	}
	
	public byte rtl(byte i) {
		byte ori = i;
		if((i -= offset) < 0)
			i+=size;

		i = (byte) ((rtl[i] + offset)%size);
		if(Enigma.DEBUG)
			System.out.printf("%d mapped to %d%n", ori, i);
		return i;
	}
	
	public String toString() {
		return String.format("%s%n%s%nOffset: %d", new String(ltr), new String(rtl), offset);
	}
}