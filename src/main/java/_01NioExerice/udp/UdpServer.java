package _01NioExerice.udp;

import _01NioExerice.ByteBufferUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * @author mini-zch
 * @date 2025/4/2 15:27
 */
/**
 *  1. receive方法实际上调用的是DatagramChannelImpl#receive(java.nio.ByteBuffer)————用于通过这个通道接收数据
 *      #如果是阻塞模式、或者有一个数据报可用，这个方法会将数据包拷贝到指定的字节缓冲区并返回；
 *       如果在非阻塞模式 并且 当前没有数据报可用，该方法立马返回，返回值为null
 *      #数据报拷贝到指定的缓冲区就像读方法一样。如果没有足够的缓冲区来容纳，数据报超出缓冲区的内容就被丢弃
 *      #这个方法会执行安全检查，类似于DatagramSocket#receive方法。此方法将验证源地址和端口号是否被管理器(security manager)的
 *       的checkAccept方法允许。
 *      #这个方法可以在任何时候调用，但是如果另一个线程已经在这个channel初始化了一个读操作，这个线程调用receive时将会阻塞，直到那个
 *       线程的读操作完成。如果这个channel's socket未绑定，则这个方法首先会将socket绑定到随机分配的地址————此操作等效于调用bind方
 *       法时参数是null。
 * */
@Slf4j
public class UdpServer {
    public static void main(String[] args) throws IOException {
        DatagramChannel channel = DatagramChannel.open();
        channel.bind(new InetSocketAddress(9999));
        System.out.println("waiting...");
        ByteBuffer buffer = ByteBuffer.allocate(6); //如果客户端消息太长，则只会返回前面的6个字节
//        System.out.println(channel.socket());
        channel.receive(buffer);
        buffer.flip();
        ByteBufferUtil.debugRead(buffer);
    }
}
