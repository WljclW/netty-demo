package netty_base.testSendRevMsg;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;
import netty_base.future_promise.TestNettyPromise;
import org.slf4j.Logger;

@Slf4j
public class EventLoopClient {
    public static void main(String[] args) throws InterruptedException {
        Logger log = org.slf4j.LoggerFactory.getLogger(EventLoopClient.class);

        ChannelFuture channelFuture = new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new StringEncoder());
                    }
                })
                /*connect异步非阻塞，main线程发起调用，但是真正执行connet的是nio线程*/
                .connect("127.0.0.1", 8080);
        /*注意：上面的连接返回的是一个ChannelFuture，这是一个异步结果。

        因此有下面的结论：
            Channel channel = channelFuture.channel(); // 1.错误的。这样写的话就是mai线程同步去获
        取channel，但是这个channelFuture是异步的，此时channel还没有完成创建
        处理的办法:1.使用sync方法同步等待处理结果;2.事件监听器*/

        /*处理办法1：使用 sync 同步处理结果*/
        /*
        channelFuture.sync(); //阻塞当前线程，直到nio线程建立连接完成
        Channel channel = channelFuture.channel();
        */

        /*处理方法2：使用 addListener(回调对象) 方法返回异步结果*/
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                Channel channel = future.channel();
                log.debug("{}",channel);
                channel.writeAndFlush("hello nio线程执行");
            }
        });

//        channel.writeAndFlush("hello,world，主线程执行");
    }
}
