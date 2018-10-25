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
	
	public static void main(String[] args) throws IOException {
		int code = JOptionPane.showOptionDialog(null, null, "Enigma 1.2", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"Encode", "Decode"}, "Encode");
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
		
		FileInputStream fis = new FileInputStream(f);
		
		long seed = new Random().nextLong() >>> 16;
		Machine machine = new Machine(seed);
		
		File outFile = new File(f.getParentFile(), f.getName()+ ".lck");
		FileOutputStream fos = new FileOutputStream(outFile);
		
		ByteBuffer buf = ByteBuffer.allocate(8);
		buf.putLong(seed);
		fos.write(buf.array());
		
		byte[] buffer = new byte[4096];
		int len = 0;
		while((len = fis.read(buffer)) > 0) {
			machine.encode(buffer, len);
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
		
		//Do not decode a file that isn't locked.
		//This is the reason we aren't encoding encoded files.
		//The program removes the .lck when it's done decoding.
		if(!f.getName().endsWith(".lck"))
			return; 
		
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
		
		Machine machine = new Machine(seed);
		machine.adjust(size<<1);
		
		//Let's just hope the size of the file is less than Integer.MAX_VALUE;
		byte[] buf = new byte[(int)size];
		fis.read(buf);
		fis.close();
		machine.decode(buf);
		
		File outFile = new File(f.getParentFile(), f.getName().substring(0, f.getName().length()-4));
		FileOutputStream fos = new FileOutputStream(outFile);
		
		fos.write(buf);
		fos.flush();
		fos.close();
		
		f.delete();
	}
}

class Machine{
	private Rotor left, mid, right, reflect;
	private Random rand;
	
	public Machine(long seed) {
		this(new Random(seed));
	}

	public Machine(Random rand) {
		this.rand = rand;
		
		left = new Rotor(shuffle()).init(rand.nextInt(16));
		mid = new Rotor(shuffle()).init(rand.nextInt(16));
		right = new Rotor(shuffle()).init(rand.nextInt(16));
		reflect = new Rotor(getRefl());
	}
	
	public byte encode(byte b) {
		byte lb = (byte) ((b & 0xF0) >>> 4);
		byte rb = (byte) (b & 0xF);
		
		lb = translate(lb, true);
		rb = translate(rb, true);
		
		return (byte) (lb << 4 | rb);
	}
	
	public void encode(byte[] in, int len) {
		for(int i=0;i<in.length && i < len;i++)
			in[i] = encode(in[i]);
	}
	
	public byte decode(byte b) {
		byte lb = (byte) ((b & 0xF0) >>> 4);
		byte rb = (byte) (b & 0xF);

		rb = translate(rb, false);
		lb = translate(lb, false);
		
		return (byte)(lb << 4 | rb); 
	}
	
	public void decode(byte[] in){
		for(int i=in.length-1;i>=0;i--)
			in[i] = decode(in[i]);
	}
	
	public byte translate(byte b, boolean forward) {
		if(!forward)
			if(right.rotateBack() && mid.rotateBack() && left.rotateBack());
		
		b = left.ltr(b);
		b = mid.ltr(b);
		b = right.ltr(b);
		b = reflect.ltr(b);
		b = right.rtl(b);
		b = mid.rtl(b);
		b = left.rtl(b);
		
		if(forward) if(right.rotate() && mid.rotate() && left.rotate());
		return b;
	}
	
	public void adjust(long by) {
		left.rotate(mid.rotate(right.rotate(by)));
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
}

class Rotor{
	
	private byte[] ltr;
	private byte[] rtl;
	
	private int size;
	private int offset;
	
	public Rotor(byte... in) {
		if(in.length != 16)
			System.out.println("Rotors have to be size 16");
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
	
	/**
	 * @param times
	 * @return The number of times it passed 0. (i.e. the number of times the next rotor should be rotated)
	 */
	public long rotate(long times) {
		int ori = offset;
		offset = (int) ((offset + times) % size);
		return times/size + (offset < ori ? 1 : 0);
	}
	
	public boolean rotate() {
		offset = ++offset % size;
		return offset == 0;
	}
	
	public boolean rotateBack() {
		if(--offset < 0) offset+=size;
		return offset == size-1;
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