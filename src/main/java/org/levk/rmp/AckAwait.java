package org.levk.rmp;

import java.util.Arrays;

class AckAwait {
    private static final long retryDelay = 3000;
    private static final short attemptMax = 11;

    private final Packet packet;
    private short attempts;
    private long lastTry;

    AckAwait(Packet packet) {
        this.packet = packet;
        this.attempts = 0;
        this.lastTry = 0;
    }

    boolean toRetry() {
        return ((lastTry + retryDelay) <= System.currentTimeMillis());
    }

    boolean toRemove() {
        return attempts >= attemptMax;
    }

    byte[] attempt() {
        attempts++;
        lastTry = System.currentTimeMillis();

        if (packet instanceof DataPacket) {
            return ((DataPacket) packet).getEncoded();
        } else {
            return ((Fragment) packet).getEncoded();
        }
    }

    boolean idCheck(byte[] id) {
        if (packet instanceof DataPacket) {
            return Arrays.equals(((DataPacket) packet).getId(), id);
        } else {
            return Arrays.equals(((Fragment) packet).getId(), id);
        }
    }
}
