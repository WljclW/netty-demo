package netty_base.testSendRevMsg;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import netty_base.future_promise.TestNettyPromise;
import org.slf4j.Logger;

import java.util.Scanner;

/**
 * 1. 实现将客户端的输入传给服务端，并且如果输入的是q，则表示停止本次消息发送
 * 2. 在1的基础上，思考如果想实现通道优雅关闭，怎么做？？
 *      并且 位置1 和 位置2 都是错误的，为什么
 * 3. 理解close操作的异步性————因此要是想在关闭后做什么事，要麽就是主线程阻塞等
 *  待 真的关闭了 即方式1；要麽就是通过监听器(告诉执行close操作的线程，close完成
 *  后做什么事) 即方式2
 * */
@Slf4j
public class EventLoopClientExample {
    public static void main(String[] args) throws InterruptedException {
        Logger log = org.slf4j.LoggerFactory.getLogger(EventLoopClientExample.class);

        NioEventLoopGroup group = new NioEventLoopGroup();
        ChannelFuture channelFuture = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                        ch.pipeline().addLast(new StringEncoder());
                    }
                })
                .connect("localhost", 8080);
        Channel channel = channelFuture.sync().channel();
        new Thread(()->{
            Scanner scanner = new Scanner(System.in);
            while(true){
                String line = scanner.nextLine();
                if("q".equals(line)){
                    channel.close();
                    /*
                    *如果想在下面的位置添加通道关闭后的处理逻辑，是错误的。
                    *原因：因为虽然是在input线程中执行close()，但是真正执行通道关闭的操
                    *   作是在nioEventLoopGroup，并且close也是异步操作。
                    * */
                    log.debug("关闭通道后的处理逻辑........"); //位置1
                    break;
                }
                channel.writeAndFlush(line);
            }
        },"inpit").start();

        /*
        * 如果想在下面的位置添加通道关闭后的处理逻辑，是错误的。
        * 原因：通道的关闭是在新的线程(名字为input的线程)，但是这里的位置是main线程执行
        *    的，因此并不能保证下面的代码 是在 input线程关闭通道后 执行的。
        * */
        log.debug("代表通道关闭后的逻辑处理......."); //位置2


        /*
        * 正确的处理方式1：同步等待
        * 这种方式处理 关闭后的逻辑 是在主线程(main线程)中执行的
        * */
        /*
        ChannelFuture closeFuture = channel.closeFuture();
        closeFuture.sync();
        log.debug("正确的处理方式1：处理通道关闭后的逻辑..........");
         */

        /*
        * 正确的处理方式2：异步
        *  并且这种方式下，处理 关闭后的逻辑 是在nioEventLoopGroup中执行的
        * */
        ChannelFuture closeFuture = channel.closeFuture();
        closeFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                log.debug("正确的处理方式2：处理关闭后的操作。。。。");
                /*如果不调用下面的方法，会发现主线程已经结束了；上面input线程也已经结束了，
                * 但是程序却没有停止。
                * 原因：上面的NioEventLoopGroup并没有停止。因此对于客户端来说，通道关闭后
                *   需要将NioEventLoopGroup也优雅的关闭*/
                group.shutdownGracefully();
            }
        });
    }
}
