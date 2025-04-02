package c4.udp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;

/**
 * @author mini-zch
 * @date 2025/4/2 15:31
 */
public class UdpClient {
    public static void main(String[] args) throws IOException {
        DatagramChannel channel = DatagramChannel.open();
        ByteBuffer buffer = StandardCharsets.UTF_8.encode("hello world");
        InetSocketAddress addr = new InetSocketAddress("localhost",9999);
        channel.send(buffer, addr);
    }
}
