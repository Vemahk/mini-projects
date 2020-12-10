package me.vem.cipher;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * It's a nibble of an enigma, ain't it? Heh. I think I'm funny.
 * 
 * This program accepts a file (or directory of files) and runs a nibble (hex I
 * suppose) level encryption on it using the idea of the Enigma Machine used by
 * Germany in WWII.
 * 
 * @author Samuel
 */
public class Program {

    public static void main(String[] args) throws IOException {
        int code = JOptionPane.showOptionDialog(null,                                // Parent
                                                null,                                // Message
                                                "Enigma 1.3",                        // Title
                                                JOptionPane.YES_NO_OPTION,           // Selection Type
                                                JOptionPane.QUESTION_MESSAGE,        // Message Type
                                                null,                                // Icon
                                                new String[] { "Encode", "Decode" }, // Options
                                                "Encode");                           // Default Option
        
        if (code == JOptionPane.CLOSED_OPTION)
            return;

        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File(System.getProperty("user.home")));
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        File f = null;
        if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
            f = fc.getSelectedFile();

        if (f == null || !f.exists())
            return;

        if (code == 0)
            encode(f);
        else if (code == 1)
            decode(f);
    }

    /**
     * Runs a hex level encryption on the given file, or if the given file object
     * represents a directory, then it recursively runs the encryption on all
     * sub-files. <br>
     * Note this method will not encrypt the file if it sees that the file is
     * already encrypted. The way it tells is if the file extension is ".lck".
     * 
     * @param f The given file or directory to encrypt.
     * @throws IOException If there are any problems in regards to file io. Could be
     *                     either that the file doesn't exist or there was a
     *                     read/write error.
     */
    public static void encode(File f) throws IOException {

        if (f.isDirectory()) {
            for (File x : f.listFiles())
                encode(x);
            return;
        }

        // Do not re-encode an encoded file. Not that it isn't possible, just that it is
        // without the design of this program to do so.
        if (f.getName().endsWith(".lck"))
            return;

        RandomAccessFile raf = new RandomAccessFile(f, "rw");
        byte[] seed = new byte[8];
        new Random().nextBytes(seed);
        
        EnigmaCipher cipher = (EnigmaCipher)new EnigmaCipher().setup(seed);

        byte[] buffer = new byte[4096];
        int len = 0;
        while ((len = raf.read(buffer)) > 0) {
            cipher.translate(buffer, len);

            raf.seek(raf.getFilePointer() - len);
            raf.write(buffer, 0, len);
        }

        // Write the seed to the end of the file.
        raf.write(seed);
        raf.close();

        f.renameTo(new File(f.getParentFile(), f.getName() + ".lck"));
    }

    /**
     * Decodes an encrypted file, or sub-files of a directory, that were encrypted
     * using the above function. <br>
     * Note that the file will be decoded if this method does not recognize it as
     * encrypted. The way this method tells is if the extension is ".lck".
     * 
     * @param f The file to decode.
     * @throws IOException If there is any io problem. This could be that the file
     *                     does not exist, or there was a problem while
     *                     read/writing.
     */
    public static void decode(File f) throws IOException {

        if (f.isDirectory()) {
            for (File x : f.listFiles())
                decode(x);
            return;
        }

        // Do not decode a file that isn't locked.
        // This is the one of the reasons we aren't encoding encoded files.
        // The program removes the .lck when it's done decoding.
        if (!f.getName().endsWith(".lck"))
            return;

        RandomAccessFile raf = new RandomAccessFile(f, "rw");

        // Read the seed from the end of the file.
        raf.seek(raf.length() - 8);
        byte[] seed = new byte[8];
        raf.read(seed);

        raf.setLength(raf.length() - 8);
        raf.seek(0);

        EnigmaCipher cipher = new EnigmaCipher();
        cipher.setup(seed);

        byte[] buf = new byte[4096];
        int len = 0;
        while ((len = raf.read(buf)) > 0) {
            cipher.translate(buf, len);

            raf.seek(raf.getFilePointer() - len);
            raf.write(buf, 0, len);
        }

        raf.close();

        // Rename the file to exclude the .lck extension.
        String fName = f.getName();
        fName = fName.substring(0, fName.lastIndexOf('.'));
        f.renameTo(new File(f.getParentFile(), fName));
    }
}