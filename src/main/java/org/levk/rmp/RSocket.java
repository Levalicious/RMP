package org.levk.rmp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class RSocket {
    private DatagramChannel socket;
    private ByteBuffer buf;

    private Queue<AckAwait> unAcked;

    private Queue<DataPacket> toSend;
    private Queue<byte[]> received;

    public RSocket(SocketAddress addr, int port) throws IOException {
        this.socket = DatagramChannel.open();
        this.socket.configureBlocking(false);
        this.socket.socket().bind(new InetSocketAddress(InetAddress.getLocalHost(), port));
        this.socket.connect(addr);
        this.buf = ByteBuffer.allocate(1500);

        this.unAcked = new LinkedList<>();

        this.toSend = new LinkedList<>();
        this.received = new LinkedList<>();
    }

    public void send(byte[] data) {
        DataPacket packet = new DataPacket(data, false);
        toSend.offer(packet);
    }

    public byte[] receive() {
        return received.poll();
    }

    public void run() {
        try {
            runSend();
            runReceive();
        } catch (Exception e) {

        }
    }

    private void runSend() throws IOException {
        if (!toSend.isEmpty()) unAcked.offer(new AckAwait(toSend.poll()));
        if (!unAcked.isEmpty()) {
            Iterator<AckAwait> iter = unAcked.iterator();
            while (iter.hasNext()) {
                AckAwait await = iter.next();

                if (await.toRemove()) {
                    iter.remove();
                } else if (await.toRetry()) {
                    buf.put(await.attempt().getEncoded());
                    buf.flip();
                    socket.write(buf);
                    buf.clear();
                }
            }
        }

        buf.clear();
    }

    private void runReceive() throws IOException {
        socket.read(buf);
        buf.flip();

        if (buf.remaining() > 0) {
            byte[] dat = new byte[buf.remaining()];
            buf.get(dat);
            buf.clear();

            Flag temp = new Flag(dat[0]);

            if (temp.testBit(0)) {
                /* Receives data, immediately sends ack */
                DataPacket packet = new DataPacket(dat);
                received.offer(packet.getPayload());

                buf.put(new AckPacket(packet.getId(), false).getEncoded());
                buf.flip();

                socket.write(buf);
                buf.clear();
            } else {
                AckPacket packet = new AckPacket(dat);
                unAcked.removeIf((a) -> a.idCheck(packet.getAck()));
            }
        }

        buf.clear();
    }
}
