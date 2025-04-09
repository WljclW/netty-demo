package _02netty_base.pipeline;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

@Slf4j
public class TestPipeline {
    public static void main(String[] args) {
        Logger log = org.slf4j.LoggerFactory.getLogger(TestPipeline.class);

        new ServerBootstrap()
                .group(new NioEventLoopGroup(),new NioEventLoopGroup(2))
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        /*首先拿到pipeline()，然后添加处理器————handler*/
                        ChannelPipeline pipeline = ch.pipeline();
                        /*依次添加处理器...处理器链是双向链表，并且netty会自动添加head和tail两个handler
                        * InboundHandler是按照添加的顺序执行的，比如下面的顺序就是handler01->handler02->handler03;
                        * OutboundHandler是按照添加顺序的倒序执行的，比如下面的程序添加的顺序是handler04->handler05
                        *   ->handler06，实际上执行的顺序是handler06->handler05->handler04
                        * */
                        pipeline.addLast("handler01",new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.debug("handler01");
                                super.channelRead(ctx, msg);
                            }
                        });
                        pipeline.addLast("handler02",new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.debug("handler02");
                                /*将数据传递给下一个handler...下面的两种方式都可以。
                                *   如果不继续传递，则处理器链从这里就断开了。。。类似于doFilter().
                                *   比如：如果这里handler02不继续向后传递，则后面的handler03~hanlder06都不会被执行*/
//                                super.channelRead(ctx, msg); //方法1
                                ctx.fireChannelRead(msg); //方法2
                            }
                        });
                        pipeline.addLast("handler02.5",new ChannelOutboundHandlerAdapter(){
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                log.debug("handler02.5");
                                super.write(ctx, msg, promise);
                            }
                        });
                        pipeline.addLast("handler03",new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.debug("handler03");
                                super.channelRead(ctx, msg);
                                /*
                                * 说明：ctx和ch在调用writeAndFlush()方法时，是有区别的。
                                *   ctx是从当前的handler依次往前找outboundHandler。因此客户端发来信息时流程是：
                                * handler01->handler02->handler03->handler02.5.
                                *   ch调用writeAndFlush()时，是直接从末尾开始倒着找outboundHandler的，因此如果
                                * 这里使用ch.writeAndFlush，则handler的顺序：handler01->handler02->handler03->
                                * handler06->handler05->handler04->handler02.5
                                * */
//                                ctx.writeAndFlush(ctx.alloc().buffer().writeBytes("hello".getBytes()));
                                ch.writeAndFlush(ctx.alloc().buffer().writeBytes("hello".getBytes()));
                            }
                        });
                        pipeline.addLast("handler04",new ChannelOutboundHandlerAdapter(){
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                log.debug("handler04");
                                super.write(ctx, msg, promise);
                            }
                        });
                        pipeline.addLast("handler05",new ChannelOutboundHandlerAdapter(){
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                log.debug("handler05");
                                super.write(ctx, msg, promise);
                            }
                        });
                        /*出现重名的handler时启动时不会报错。但是客户端发来消息处理时会报错："Duplicate handler name: handler04"*/
                        pipeline.addLast("handler06",new ChannelOutboundHandlerAdapter(){
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                log.debug("handler06");
                                super.write(ctx, msg, promise);
                            }
                        });
                    }
                })
                .bind(8080);
    }
}
