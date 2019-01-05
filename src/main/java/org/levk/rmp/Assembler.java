package org.levk.rmp;

import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;

import static org.levk.rmp.RSocket.MAX_WAIT_MOD;

public class Assembler {
    private byte[][] packets;
    private byte[] messageId;
    private int awaiting;
    private int maxAge;

    public Assembler(Fragment fragment, int currentAge) {
        this.messageId = fragment.getMessageId();

        awaiting = byteArrayToInt(fragment.getMaxIndex());
        this.packets = new byte[awaiting + 1][];

        packets[byteArrayToInt(fragment.getIndex())] = fragment.getPayload();
        maxAge = (awaiting + 1) * MAX_WAIT_MOD + currentAge;
    }

    public void addFragment(Fragment fragment, int currentAge) {
        if (packets[byteArrayToInt(fragment.getIndex())] == null) {
            packets[byteArrayToInt(fragment.getIndex())] = fragment.getPayload();
            awaiting--;
            maxAge = (awaiting + 1) * MAX_WAIT_MOD + currentAge;
        }
    }

    public boolean isComplete() {
        return (awaiting == 0);
    }

    public byte[] getMessage() {
        return merge(packets);
    }

    public boolean toDelete(int currentAge) {
        return currentAge > maxAge;
    }

    public int packetCount() {
        return packets.length;
    }

    private static int byteArrayToInt(byte[] b) {
        if (b == null || b.length == 0)
            return 0;
        return new BigInteger(1, b).intValue();
    }

    public static byte[] merge(byte[]... arrays)
    {
        int count = 0;
        for (byte[] array : arrays)
        {
            count += array.length;
        }

        // Create new array and copy all array contents
        byte[] mergedArray = new byte[count];
        int start = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, mergedArray, start, array.length);
            start += array.length;
        }
        return mergedArray;
    }
}
