package _03netty_partialAndMerging_Packet.dilimeter;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.Random;

/**
 * []：客户端以"\n"作为消息结尾的标志向服务端发送任意长度的消息(下面的例子中最
 *  大长度是256)...在打印出来的信息中"."就代表消息结尾的"\n"(\n是打印不出来的)。
 * 总结：能解决。但是需要一个个的遍历消息的字节来拆分出消息，效率低
 * @author mini-zch
 * @date 2025/4/8 17:20
 */
public class Client {
    public static void main(String[] args) {
        send();
        System.out.println("==============finish============");
    }

    public static void send() {
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(worker);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            ByteBuf buf = ctx.alloc().buffer();
                            char c = '0';
                            Random random = new Random();
                            for (int i = 0; i < 10; i++){
                                StringBuilder sb = makeString(c, random.nextInt(256) + 1);
                                c++;
                                buf.writeBytes(sb.toString().getBytes());
                            }
                            ctx.writeAndFlush(buf);
                        }
                    });
                }
            });
            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 8080).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            worker.shutdownGracefully();
        }
    }

    /**
     * 根据字节c以及len构造出以“\n”结尾的字符串
     * */
    public static StringBuilder makeString(char c,int len){
        StringBuilder sb = new StringBuilder(len + 2);
        for (int i = 0; i < len; i++){
            sb.append(c);
        }
        sb.append("\n");
        return sb;
    }
}
