package c4.BaseServerModel;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

import static c4.ByteBufferUtil.debugRead;

/**
 * 非阻塞模式的服务端：
 *   1.与阻塞模式的区别在于：
 *      ServerSocketChannel.configureBlocking配置服务端是非阻塞的。结果就是accept()方法即使没有
 *    客户端请求连接也会立即返回，返回null；否则的话就返回和客户端的连接通道——SocketChannel。
 *      SocketChannel.configureBlocking配置客户端通道为非阻塞的。结果就是read()方法立即返回，即
 *    使没有数据也会理解返回0，否则返回可读的字节数.
 *   2.程序的直观现象：
 *      while循环一直在执行，没有任何的阻塞；日志在不断的打印。
 *      【总结】线程不会被阻塞，也不会被挂起。而是不终止的运行————更准确的说其实是空转，没有做有意义的事。
 * */
@Slf4j
public class ServerNonBlock {
    public static void main(String[] args) throws IOException {
        org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ServerNonBlock.class);

        ByteBuffer buffer = ByteBuffer.allocate(16);
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.bind(new InetSocketAddress(8080));
        ArrayList<SocketChannel> channels = new ArrayList<>();
        while (true){
            log.debug("connecting.......");
            SocketChannel sc = ssc.accept(); //非阻塞模式下立即返回，没有客户端连接则返回null
            if(sc!=null){
                sc.configureBlocking(false);
                log.debug("connected......{}",sc);
                channels.add(sc);
            }
            for(SocketChannel channel:channels){
                log.debug("before read........{}",channel);
                int read = channel.read(buffer);//非阻塞模式下立即返回，如果没有数据则返回0
                log.debug("read {}",read);
                if(read>0){
                    buffer.flip();
                    debugRead(buffer);
                    buffer.clear();
                    log.debug("after read........{}",channel);
                }
            }
        }
    }
}
