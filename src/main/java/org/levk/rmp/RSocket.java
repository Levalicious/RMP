package org.levk.rmp;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

class RSocket {
    private DatagramChannel socket;
    private ByteBuffer buf;

    private Queue<AckAwait> unAcked;

    private Queue<Packet> toSend;
    private Queue<byte[]> received;

    private HashMap<Integer, Assembler> assemblers;

    private int receivedCount;
    public static final int MAX_WAIT_MOD = 3;

    private int sendFails;
    private int receiveFails;

    private static final int MTU = 1472;

    public RSocket(SocketAddress addr, int port) throws IOException {
        this.socket = DatagramChannel.open();
        this.socket.configureBlocking(false);
        this.socket.socket().bind(new InetSocketAddress(InetAddress.getLocalHost(), port));
        this.socket.connect(addr);
        this.buf = ByteBuffer.allocate(1500);

        this.unAcked = new LinkedList<>();

        this.assemblers = new HashMap<>();

        this.toSend = new LinkedList<>();
        this.received = new LinkedList<>();

        receivedCount = 0;
        sendFails = 0;
        receiveFails = 0;
    }

    public void send(byte[] data) {
        if (data.length > (MTU - 5)) {
            fragment(data);
        } else {
            DataPacket packet = new DataPacket(data, false);
            toSend.offer(packet);
        }
    }

    private void fragment(byte[] data) {

        byte[][] partedData = partition(data, (MTU - 13));

        byte[] messageID = new byte[4];
        ThreadLocalRandom.current().nextBytes(messageID);

        System.out.println("Fragmented and sent data in " + partedData.length + " packets");

        int currentIndex = 0;
        byte[] maxIndex = Arrays.copyOfRange(intToBytes(partedData.length - 1), Integer.BYTES - 2, Integer.BYTES);
        System.out.println(partedData.length);

        for (byte[] dat : partedData) {
            Fragment fragment = new Fragment(dat, messageID, Arrays.copyOfRange(intToBytes(currentIndex), Integer.BYTES - 2, Integer.BYTES), maxIndex);
            toSend.offer(fragment);
            currentIndex++;
        }
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
                    sendFails++;
                } else if (await.toRetry()) {
                    buf.put(await.attempt());
                    buf.flip();
                    socket.write(buf);
                    buf.clear();
                }
            }
        }

        buf.clear();
    }

    private void checkAssemblers() {
        if (!assemblers.isEmpty()) {
            Iterator<Assembler> iter = assemblers.values().iterator();
            while (iter.hasNext()) {
                Assembler assembler = iter.next();

                if (assembler.toDelete(receivedCount)) {
                    iter.remove();
                    receiveFails++;
                }
            }
        }
    }

    private void runReceive() throws IOException {
        socket.read(buf);
        buf.flip();

        if (buf.remaining() > 0) {
            byte[] dat = new byte[buf.remaining()];
            buf.get(dat);
            buf.clear();

            Flag temp = new Flag(dat[0]);

            /* Checks if data packet */
            if (temp.testBit(0)) {
                /* Checks if fragment */
                if (temp.testBit(1)) {
                    Fragment fragment = new Fragment(dat);

                    int id = byteArrayToInt(fragment.getMessageId());

                    if (assemblers.containsKey(id)) {
                        assemblers.get(id).addFragment(fragment, receivedCount);
                    } else {
                        assemblers.put(id, new Assembler(fragment, receivedCount));
                    }

                    if (assemblers.get(id).isComplete()) {
                        received.offer(assemblers.get(id).getMessage());
                        System.out.println("Received and reassembled data from " + assemblers.get(id).packetCount() + " packets.");
                        assemblers.remove(id);
                    }

                    buf.put(new AckPacket(fragment.getId(), false).getEncoded());
                    buf.flip();

                    socket.write(buf);
                    buf.clear();
                } else {
                    /* Receives data, immediately sends ack */
                    DataPacket packet = new DataPacket(dat);
                    received.offer(packet.getPayload());

                    buf.put(new AckPacket(packet.getId(), false).getEncoded());
                    buf.flip();

                    socket.write(buf);
                    buf.clear();
                }
                receivedCount++;
            } else {
                AckPacket packet = new AckPacket(dat);
                unAcked.removeIf((a) -> a.idCheck(packet.getAck()));
            }
        }

        buf.clear();
    }

    private static int byteArrayToInt(byte[] b) {
        if (b == null || b.length == 0)
            return 0;
        return new BigInteger(1, b).intValue();
    }

    private static byte[] intToBytes(int val){
        return ByteBuffer.allocate(Integer.BYTES).putInt(val).array();
    }

    private static byte[][] partition(byte[] in, int partitionSize)
    {
        int partitionCount =  (int)Math.ceil((double)in.length / (double) partitionSize);

        byte[][] temp = new byte[partitionCount][];

        for (int p = 0; p < partitionCount; p++)
        {
            int start = p * partitionSize;
            int len = (p != partitionCount - 1) ? partitionSize : in.length - start;
            byte[] partition = new byte[len];

            System.arraycopy(in, start, partition, 0, len);

            temp[p] = partition;
        }

        return temp;
    }
}
