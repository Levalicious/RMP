package org.levk.rmp;

import java.security.SecureRandom;

public class DataPacket extends Packet {
    private final static SecureRandom rand = new SecureRandom();

    private Flag flag = new Flag();
    private byte[] id = new byte[4];
    private byte[] payload;

    private byte[] encoded;

    public DataPacket(byte[] payload, boolean unencoded) {
        rand.nextBytes(id);

        /* Marks this packet as a data packet */
        flag.setBit(0, true);

        this.payload = payload;

        this.encoded = new byte[payload.length + 5];

        encoded[0] = flag.getByte();
        System.arraycopy(id, 0, encoded, 1, 4);
        System.arraycopy(payload, 0, encoded, 5, payload.length);
    }

    public DataPacket(byte[] encoded) {
        this.encoded = encoded;

        flag = new Flag(encoded[0]);

        System.arraycopy(encoded, 1, id, 0, 4);
        payload = new byte[encoded.length - 5];
        System.arraycopy(encoded, 5, payload, 0, payload.length);
    }

    public byte[] getId() {
        return id;
    }

    public byte[] getPayload() {
        return payload;
    }

    public byte[] getEncoded() {
        return encoded;
    }
}
