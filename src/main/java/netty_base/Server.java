package netty_base;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author mini-zch
 * @date 2025/4/3 14:13
 */
@Slf4j
public class Server {
    public static void main(String[] args) {
        /*1. 创建服务端的启动类*/
        new ServerBootstrap()
                /*2. 指定用于parent(accept)以及child(client)的EventLoopGroup*/
                .group(new NioEventLoopGroup())
                /*3. 使用channel 或者 ChannelFactory 创建通道实例*/
                .channel(NioServerSocketChannel.class)
                /*4. 与客户端连接后，如何初始化和该客户端连接相关的pipeline*/
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        // 添加一个解码器。将ByteBuffer内容解码为字符串
                        ch.pipeline().addLast(new StringDecoder());
                        // 添加一个自定义的handler
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                            /*
                            ctx：ChannelHandlerContext 是 Netty 中每个 ChannelHandler 的上下文对象，它用于管理与通道相关
                                的信息，允许调用者向后传递事件或消息。
                            msg：由于前面的handler已经完成了内容的解码，因此这里会拿到源字符串*/
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                super.channelRead(ctx, msg);
                                System.out.println("接收到的消息2......"+msg);
                            }
                        });
                    }
                })
                .bind(8080);
    }
}
