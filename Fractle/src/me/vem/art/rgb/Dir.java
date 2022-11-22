package me.vem.art.rgb;

public enum Dir{
    UL(-1, -1, 0),
    U (0,  -1, 1),
    UR(1,  -1, 2),
    R (1,   0, 3),
    DR(1,   1, 4),
    D (0,   1, 5),
    DL(-1,  1, 6),
    L (-1,  0, 7);
    
    public static final Dir[] vals = Dir.values();
    
    int dx, dy, bn;
    
    private Dir(int dx, int dy, int bn) {
        this.dx = dx;
        this.dy = dy;
        this.bn = bn;
    }

    public Dir opp() { return vals[(bn+4)%8]; }
}