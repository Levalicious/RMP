package org.levk.rmp;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) throws Exception {
        String s = "Hi how many bytes can I expect this to be now that it's a huge fuckin string in UTF 16 so that I can get as many bytes out of this bitch as possible? Brother, may I have some BYTES update: I need more bytes because this string isn't providing enough so I guess I'll just keep typing some bullshit until I have more than 1467 bytes so that the message will actually get split among two packets update still not enough in this string to be that many bytes but I'm slowly getting there, last I checked I hit 786 bytes now I'll check again 1072 bytes now, but it's still not enough I need MOAR BYTES GIVE ME THE BYTES BROTHER too bad I don't know how to type the umlaut thing above a letter in java 1390 bytes now I'll be there in a hot second IM COMIN FOR YOU YOU BITCH ASS BYTE COUNT NOW BOW TO ME BITCHHHH awesome 1648 bytes";
        byte[] str = s.getBytes(StandardCharsets.UTF_16);
        System.out.println(str.length);

        RSocket socket = new RSocket(new InetSocketAddress(InetAddress.getLocalHost(), 40420), 40424);

        socket.send(str);

        while (true) {
            socket.run();
        }
    }
}
