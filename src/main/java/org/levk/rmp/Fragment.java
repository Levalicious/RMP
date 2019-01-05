package org.levk.rmp;

import java.security.SecureRandom;

class Fragment extends Packet {
    private final static SecureRandom rand = new SecureRandom();

    private Flag flag = new Flag();
    private byte[] id = new byte[4];
    private byte[] messageId = new byte[4];
    private byte[] index = new byte[2];
    private byte[] maxIndex = new byte[2];
    private byte[] payload;

    private byte[] encoded;

    public Fragment(byte[] payload, byte[] messageId, byte[] index, byte[] maxIndex) {
        rand.nextBytes(id);

        /* Marks this packet as a data packet */
        flag.setBit(0, true);

        /* Marks this packet as a fragment */
        flag.setBit(1, true);

        this.payload = payload;

        this.messageId = messageId;
        this.index = index;
        this.maxIndex = maxIndex;

        this.encoded = new byte[payload.length + 13];
        encoded[0] = flag.getByte();
        System.arraycopy(id, 0, encoded, 1, 4);
        System.arraycopy(messageId, 0, encoded, 5, 4);
        System.arraycopy(index, 0, encoded, 9, 2);
        System.arraycopy(maxIndex, 0, encoded, 11, 2);
        System.arraycopy(payload, 0, encoded, 13, payload.length);
    }

    public Fragment(byte[] encoded) {
        this.encoded = encoded;

        flag = new Flag(encoded[0]);

        System.arraycopy(encoded, 1, id, 0, 4);
        System.arraycopy(encoded, 5, messageId, 0, 4);
        System.arraycopy(encoded, 9, index, 0, 2);
        System.arraycopy(encoded, 11, maxIndex, 0, 2);
        payload = new byte[encoded.length - 13];
        System.arraycopy(encoded, 13, payload, 0, payload.length);
    }

    public byte[] getId() {
        return id;
    }

    public byte[] getMessageId() {
        return messageId;
    }

    public byte[] getIndex() {
        return index;
    }

    public byte[] getMaxIndex() {
        return maxIndex;
    }

    public byte[] getEncoded() {
        return encoded;
    }

    public byte[] getPayload() {
        return payload;
    }
}
