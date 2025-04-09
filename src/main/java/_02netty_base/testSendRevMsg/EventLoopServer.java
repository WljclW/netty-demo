package _02netty_base.testSendRevMsg;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;

/**
 * server端的优化：
 * 1. 创建两个EventLoopGroup，一个负责处理accept事件，一个负责处理读写事件
 *      ————.group(new NioEventLoopGroup(), new NioEventLoopGroup(2));
 * 2. 在Handler中，让耗时的操作放在单独的线程池中执行。优点：
 *      EventLoopGroup的一个线程一般是对应多个客户端的，如果某个客户端的操作比较耗时，就会导致绑定在这个
 *   EventLoop的其他客户端的处理变慢。
 * 3. 每一个客户端和服务端创建连接后，会绑定到EventLoopGroup中的一个线程，后续就由这个线程来处理这个客户
 *  端的事件。
 *    如果在处理过程中，耗时操作放入了额外的线程池，比如下面程序中的handler02，则客户端的请求在处理时，也
 *  会绑定这个DefaultEventLoopGroup的某一个线程
 * */
@Slf4j
public class EventLoopServer {
    public static void main(String[] args) {
        Logger log = org.slf4j.LoggerFactory.getLogger(EventLoopClient.class);

        DefaultEventLoopGroup group = new DefaultEventLoopGroup(2);
        new ServerBootstrap()
//                .group(new NioEventLoopGroup()) //这种方式下accept、read事件用的是同一个group
                /*下面的写法进一步细化：第一个负责处理accept事件；第二个负责读写事件(可以根据实际
                  指定线程池的线程数量)*/
                .group(new NioEventLoopGroup(), new NioEventLoopGroup(2))
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {

                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast("Handler01",new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf msg1 = (ByteBuf) msg;
                                log.debug(msg1.toString(StandardCharsets.UTF_8));
                                ctx.fireChannelRead(msg);
                            }
                        }).addLast(group, "Handler02", new ChannelInboundHandlerAdapter() {

                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf msg1 = (ByteBuf) msg;
                                log.debug(msg1.toString(StandardCharsets.UTF_8));
                            }
                        });
                    }
                })
                .bind(8080);
    }
}
