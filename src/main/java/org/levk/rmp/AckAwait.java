package org.levk.rmp;

import java.util.Arrays;

class AckAwait {
    private static final long retryDelay = 3000;
    private static final short attemptMax = 11;

    private DataPacket packet;
    private short attempts;
    private long lastTry;

    AckAwait(DataPacket packet) {
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

    DataPacket attempt() {
        attempts++;
        lastTry = System.currentTimeMillis();
        return packet;
    }

    boolean idCheck(byte[] id) {
        return Arrays.equals(packet.getId(), id);
    }
}
