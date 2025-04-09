package _01NioExerice.beforeSelectorThreadPool;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

/**
 * @author mini-zch
 * @date 2025/4/2 17:17
 */
public class ServerSelectorWorkerClient {
    public static void main(String[] args) throws IOException {
        SocketChannel sc = SocketChannel.open();
        sc.connect(new java.net.InetSocketAddress("localhost", 8080));
        sc.write(Charset.defaultCharset().encode("hello"));
        System.out.println("dada");
    }
}
