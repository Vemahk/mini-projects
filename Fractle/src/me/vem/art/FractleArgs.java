package me.vem.art;

public class FractleArgs {

    private final String[] args;
    
    public boolean save = false;
    public boolean repeat = false;
    public byte colorBits = 6;
    public byte NUM_START = 1;
    public int scaleDiv = 1;
    
    private boolean help = false;
    
    public FractleArgs(String... args) {
        this.args = args;
        parse();
    }
    
    public void parse() {
        for(int i=0;i<args.length;i++) {
            if("-repeat".equals(args[i]))
                repeat = true;
            if("-bits".equals(args[i]) && args.length > i + 1)
                try {
                    colorBits = Byte.parseByte(args[i+1]);
                    switch(colorBits) {
                    case 8: scaleDiv <<= 1;
                    case 7: scaleDiv <<= 2;
                    default: break;
                    }
                }catch (NumberFormatException e) { e.printStackTrace(); }
            if("-save".equals(args[i]))
                save = true;
            if("-help".equals(args[i])) {
                help = true;
                return;
            }
            if("-numstart".equals(args[i]) && args.length > i + 1)
                try {
                    NUM_START = Byte.parseByte(args[i+1]);
                }catch(NumberFormatException e) {e.printStackTrace();}
        }
    }
    
    public boolean isHelp() {
        if(help)
            System.out.printf("Available commands:%n%s%n%s%n%s%n%s%n", "-repeat", "-bits [4-8]", "-save", "-numstart [1-127]");
        
        return help;
    }
    
    public int getWidth() {
        return 1 << ((colorBits*3+1)/2);
    }
    
    public int getHeight() {
        return 1 << (colorBits * 3 / 2);
    }
}
