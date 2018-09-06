import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
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
		int code = JOptionPane.showOptionDialog(null, null, "Enigma 1.0", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"Encode", "Decode"}, "Encode");
		
		JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(new File(System.getProperty("user.home") + "/Desktop/"));
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		
		File f = null;
		if(fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
			f = fc.getSelectedFile();
		
		if(f == null || !f.exists())
			return;
		
		if(code==0) {
			if(!f.isDirectory())
				encodeFile(f);
			else encodeDir(f);
		}else if(code==1) {
			if(!f.isDirectory())
				decodeFile(f);
			else decodeDir(f);
		}
	}
	
	public static void encodeDir(File dir) throws IOException {
		for(File x : dir.listFiles())
			if(x.isDirectory())
				encodeDir(x);
			else encodeFile(x);
	}
	
	public static void decodeDir(File dir) throws IOException {
		for(File x : dir.listFiles())
			if(x.isDirectory())
				decodeDir(x);
			else decodeFile(x);
	}
	
	public static void encodeFile(File f) throws IOException {

		if(f.getName().endsWith(".lck"))
			return;
		
		FileInputStream fis = new FileInputStream(f);
		
		rand = new Random();
		long seed = 0;
		rand.setSeed(seed = rand.nextLong() >>> 16);
		
		long size = 0;
		
		l = new Rotor(shuffle()).init(rand.nextInt(16));
		m = new Rotor(shuffle()).init(rand.nextInt(16));
		r = new Rotor(shuffle()).init(rand.nextInt(16));
		ref = new Rotor(getRefl());

		if(DEBUG)
			System.out.printf("%s%n%s%n%s%n%s%n", l, m, r, ref);
		
		File tmpOut = new File(f.getParentFile(), "tmpout.dat");
		if(tmpOut.exists())
			tmpOut.delete();
		tmpOut.createNewFile();
		FileOutputStream tmpFos = new FileOutputStream(tmpOut);
		
		byte[] buffer = new byte[4096];
		int len = 0;
		while((len = fis.read(buffer)) > 0) {
			size += len;
			encode(buffer, len);
			tmpFos.write(buffer, 0, len);
		}
		
		fis.close();
		tmpFos.flush();
		tmpFos.close();
		
		File outFile = new File(f.getParentFile(), f.getName()+ ".lck");
		FileOutputStream fos = new FileOutputStream(outFile);
		FileInputStream tmpFis = new FileInputStream(tmpOut);
		
		fos.write(longToBytes(seed));
		fos.write(longToBytes(size));
		
		while((len = tmpFis.read(buffer)) > 0)
			fos.write(buffer, 0, len);

		f.delete();
		fos.flush();
		fos.close();
		tmpFis.close();
		tmpOut.delete();

	}
	
	public static void decodeFile(File f) throws IOException {

		if(!f.getName().endsWith(".lck"))
			return;
		
		FileInputStream fis = new FileInputStream(f);
		
		long seed = 0;
		long size = 0;
		
		ByteBuffer tmpbuf = ByteBuffer.allocate(16);
		
		byte[] tmpbufarr = tmpbuf.array();
		fis.read(tmpbufarr);
		seed = tmpbuf.getLong();
		size = tmpbuf.getLong();
		
		if(seed == 0) {
			System.out.println("Seed not found...");
			fis.close();
			return;
		}
		
		if(size == 0) {
			System.out.println("Size not found...");
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
		
		byte[][] data = new byte[(int) (size / 4096 + 1)][];
		
		byte[] buffer = new byte[4096];
		int len = 0;
		
		int dist = 0;
		for(;dist<data.length && (len = fis.read(buffer)) > 0; dist++) {
			data[dist] = Arrays.copyOf(buffer, len);
		} dist--;
		
		fis.close();
		
		for(int i=dist;i>=0;i--) {
			decode(data[i]);
		}
		
		File outFile = new File(f.getParentFile(), f.getName().substring(0, f.getName().length()-4));
		FileOutputStream fos = new FileOutputStream(outFile);
		
		for(int i=0;i<=dist;i++)
			fos.write(data[i]);
		
		f.delete();
		fos.flush();
		fos.close();
	}
	
	public static void encode(byte[] in, int len) {
		for(int i=0;i<in.length && i < len;i++) {
			byte left = (byte) ((in[i]&0xF0) >>> 4);
			byte right = (byte) (in[i]&0xF);
			
			left = Rotor.translate(left, l, m, r, ref, true);
			right = Rotor.translate(right, l, m, r, ref, true);
			
			if(DEBUG) System.out.println();
			
			in[i] = (byte) (left << 4 | right);
		}
	}
	
	public static void decode(byte[] in){
		for(int i=in.length-1;i>=0;i--) {
			byte left = (byte) ((in[i]&0xF0) >>> 4);
			byte right = (byte) (in[i] & 0xF);
	
			right = Rotor.translate(right, l, m, r, ref, false);
			left = Rotor.translate(left, l, m, r, ref, false);
			
			if(DEBUG) System.out.println();
			in[i] = (byte)(left << 4 | right);
		}
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
	
	private static byte[] longToBytes(long l) {
		byte[] out = new byte[8];
		for(int i=0;i<8;i++)
			out[i] = (byte)((l >>> (56 - 8*i)) & 0xFF);
		return out;
	}
	
	private static long bytesToLong(byte[] b) {
		long out = 0;
		for(int i=0;i<8;i++) 
			out += ((long)b[i] & 0xFF) << (56 - i*8);
		return out;
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
		
		if(forward) 
			if(r.rotate() && m.rotate() && l.rotate());
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