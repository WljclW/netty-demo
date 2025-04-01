package c4;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

/**
 * 1.关于异常关闭：
 *      套接字通道支持异步关闭，这类似于Channel类中指定的异步关闭操作。
 *      如果套接字的输入端被一个线程关闭，而另一个线程在套接字通道上的读取操作中被阻塞，那么被阻塞线程中的读取操作将不读
 *   取任何字节，并返回-1。
 *      如果套接字的输出端被一个线程关闭，而另一个线程在套接字通道的写操作中被阻塞，那么被阻塞的线程将收到一 个AsynchronousCloseException
 * 2.InetSocketAddress类提供了一种IP形式的Socket
 * 3.SocketChannel类的open()————内部调用的是SelectorProvider.provider().openSocketChannel()，这个方法会根据具体的系统，从而
 *      返回合适的SocketChannel。
 * 4.SocketChannel类提供了connect方法来连接服务器。关于这个方法的说明：
 *    4.1阻塞/非阻塞
 *      阻塞模式下：connect方法会阻塞，直到连接成功或者出现IO异常
 *      非阻塞模式下：connect方法会立即返回，如果连接成功，则返回true，如果连接失败，则返回false。
 *    4.2进行安全检查
 *      SecurityManager如果配置，则会调用checkConnect方法，检查是否允许连接(是否允许连接到远程的ip和端口号)
 *    4.3其他
 *      如果在connect执行过程中，该通道发生了写或者读操作，则会阻塞直到来凝结完成；
 *      如果SecurityManager检查发现被禁止、或者出现异常等，总之造成了连接失败，则通道会关闭。
 * 5.在此通道的读写操作要求，必须在connect方法成功之后调用，否则会抛出NotYetConnectedException异常
 * */
public class Client {
    public static void main(String[] args) throws IOException {
        SocketChannel sc = SocketChannel.open();
//        sc.write(Charset.defaultCharset().encode("hello")); //错误的使用。源码注释说了，不能在没有连接的情况下使用SocketChannel的io操作
        sc.connect(new InetSocketAddress("localhost",8080));
        /*在这一步设置断点。1. 可以看到当客户端connect成功之后，服务端的反应；
        *                2. 在"Evaluate expression"中“sc.write(Charset.defaultSet.encode("message"))”看到服务端的接受情况*/
        System.out.println("waiting......");
    }
}
