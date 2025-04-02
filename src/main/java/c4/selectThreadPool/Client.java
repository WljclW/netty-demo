package c4.selectThreadPool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

/**
 * @author mini-zch
 * @date 2025/4/2 20:15
 */
/**
 * 1.SocketChannel也可以调用bind方法。但是如果下面的方法中调用了bind方法，会报异常：
 *      BindException: Address already in use: bind
 * */
public class Client {
    public static void main(String[] args) throws IOException {
        SocketChannel channel = SocketChannel.open();
        channel.connect(new InetSocketAddress("localhost", 8080));
        channel.write(Charset.defaultCharset().encode("hellp"));
        System.out.println("dada");
    }
}
