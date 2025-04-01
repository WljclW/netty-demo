package c4;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * 服务端的阻塞模式的实现：
 *   1.ServerSocketChannel：调用open()创建服务端的通道，调用bind即可监听(绑定)特定的端口
 *   2.调用ServerSocketChannel.accept即可接收客户端的连接。但是如果没有客户端的连接请求，线程就
 *      会在这里阻塞住，直到有客户端请求连接；(直观的现象就是线程打印完connecting。。。就不动了)
 *   3.SocketChannel的read方法也是阻塞式的，如果没有数据就等待(阻塞)。因此观察到的现象：
 *      当有一个客户端来连接时会连接成功，服务端会打印"connected...."以及"before read....."，然
 *      后就没动静了，因为read方法阻塞住了
 *
 * */
@Slf4j
public class ServerBlock {
    public static void main(String[] args) throws IOException {
        /*注解@Slf4j配置后提示找不到log，因此换一种方式*/
        org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ServerBlock.class);

        ByteBuffer buffer = ByteBuffer.allocate(16);
        //服务端的网络通道,调用bind绑定端口
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.bind(new InetSocketAddress(8080));
        //连接的集合
        List<SocketChannel> channels = new ArrayList<>();
        //不断的接收连接请求，并处理
        while(true){
            log.debug("connecting.......");
            /*accept()方法用于客户端的连接；返回的SocketChannel用于服务端和这个客户端通信的通道*/
            SocketChannel sc = ssc.accept(); /*阻塞模式，没有连接请求到来时线程会到这里阻塞住*/
            log.debug("connected....{}",sc);
            channels.add(sc);
            for (SocketChannel channel:channels){
                //接收客户端发来的数据
                log.debug("before read........{}",channel);
                channel.read(buffer); /*read()方法也是阻塞的，没有数据就等待。直到线程停止运行*/
                buffer.flip();
                ByteBufferUtil.debugRead(buffer);
                buffer.clear();
                log.debug("after read........{}",channel);
            }
        }
    }
}
