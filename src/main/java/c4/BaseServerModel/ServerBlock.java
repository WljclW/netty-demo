package c4.BaseServerModel;

import c4.ByteBufferUtil;
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
 *     补充accept方法的源码————
 *        ①无论ServerSocketChannel是否阻塞，accept方法返回的SocketChannel都是阻塞的
 *        ②如果ServerSocketChannel是阻塞的，
 *          如果ServerSocketChannel是非阻塞的，并且没有挂起的连接请求，accept方法会立马返回null
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
            log.debug("channels的现状.......{}",channels); //打印当前的所有连接
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



/**
 * [实验总结]
 * 一、服务端初始运行：
 *      在while(true)中执行到ssc.accept()时阻塞住，会等待有客户端的连接
 * 二、客户端1开始运行：
 *      客户端会尝试连接服务端，因此服务端阻塞的ssc.accept()执行成功。对于客户端而言，连接成功了；对于服务端
 *   来说，阻塞的代码继续向后执行，会打印出channels中所有已经建立了连接的信息，继续向后执行遍历channels中所有
 *   的channel。。。由于服务端会针对每一个channel调用read()方法即channel.read(buffer)。
 *      由于当前只有一个客户端，并且没有发送数据，因此read()方法会阻塞住。【注意】客户端一先不发消息
 * 三、客户端二开始运行
 *      客户端二会执行connect尝试连接服务器。。。【注意】虽然此时服务端的程序阻塞在while(true)中的read，但是
 *   连接是可以建立成功的。
 *      【疑问】对于客户端二来说，和服务端建立连接成功了；对于服务端来说，没有任何变化，因为它的程序还是阻塞在read方法————
 *   思考：为什么服务端程序阻塞在read那里，但是客户端二还是能连接成功，或者准确的说，为什么客户端的connect方法会返
 *   返回true???
 *      现在在客户端二debug模式下发送数据。
 *      得到的现象：服务端阻塞在read方法；客户端一连接成功没有发送数据；客户端二连接成功，并且发送了数据。
 *      为什么客户端2数据发送了，但是服务端还是阻塞在read？？因为服务端遍历chanels集合，第一个拿到的是客户端一的通道，客户
 *   端一的通道没有发送数据，因此read方法阻塞住。即使客户端二发送了数据也没用。
 *      现在让客户端一发送数据。
 *      得到的现象：服务端的read方法执行成功，并拿到了客户端一发送的数据。。但是由于服务端本轮循环时客户端二还没有连接，因此
 *   会跳出for循环继续执行accept()，这个方法本来应该是阻塞的，但是前不久客户端二执行了connect来连接，因此服务端这里不会阻塞
 *   直接执行成功，然后会在channels中添加客户端二的通道即"channels.add(sc)"，在接下来打印的channels即可看到客户端二的连
 *   接信息。。。然后服务端ixu执行进入到for循环遍历channels中的channel。首先拿到的是客户端一的连接，由于客户端一没有新发的消
 *   息，因此服务端会在遍历到第一个channel的时候就阻塞。
 *      此时，如果用客户端一继续发送消息，服务端会拿到客户端一的消息；然后接着遍历第二个channel，由于之前客户端二已经发送过消息
 *   因此遍历客户端二的channel的时候，read方法不会阻塞，会直接拿到结果返回。
 *      至此，channels中的两个元素都已遍历，服务端for循环结束，再一次执行accept().......
 *
 * */
