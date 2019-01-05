package org.levk.rmp;

class AckPacket extends Packet {
    private byte[] ack_id = new byte[4];

    private byte[] encoded;

    AckPacket(byte[] id, boolean enc) {
        this.ack_id = id;
        this.encoded = new byte[5];

        Flag flag = new Flag();
        encoded[0] = flag.getByte();
        System.arraycopy(ack_id, 0, encoded, 1, 4);
    }

    AckPacket(byte[] encoded) {
        this.encoded = encoded;
        System.arraycopy(encoded, 1, ack_id, 0, 4);
    }

    byte[] getEncoded() {
        return encoded;
    }

    byte[] getAck() {
        return ack_id;
    }
}
