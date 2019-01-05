package org.levk.rmp;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class Client {
    public static void main(String[] args) throws Exception{
        RSocket socket = new RSocket(new InetSocketAddress(InetAddress.getLocalHost(), 40424), 40420);
        while (true) {
            socket.run();

            byte[] in = socket.receive();
            if (in != null) {
                System.out.println("Received: ");
                System.out.println(new String(in, StandardCharsets.UTF_16));
            }
        }
    }
}
