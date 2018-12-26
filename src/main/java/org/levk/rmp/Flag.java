package org.levk.rmp;

class Flag {
    private boolean[] bits = new boolean[8];

    Flag() {

    }

    public Flag(byte x) {
        bits[0] = ((x & 0x80) != 0);
        bits[1] = ((x & 0x40) != 0);
        bits[2] = ((x & 0x20) != 0);
        bits[3] = ((x & 0x10) != 0);
        bits[4] = ((x & 0x08) != 0);
        bits[5] = ((x & 0x04) != 0);
        bits[6] = ((x & 0x02) != 0);
        bits[7] = ((x & 0x01) != 0);
    }

    void setBit(int i, boolean val) {
        bits[i] = val;
    }

    boolean testBit(int i) {
        return bits[i];
    }

    byte getByte() {
        byte out = (byte) 0x00;
        for (int i = 0; i < 8; i++) {
            if (bits[i]) {
                out |= (128 >> i);
            }
        }

        return out;
    }
}
