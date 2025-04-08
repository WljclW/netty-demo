package netty_partialAndMerging_Packet.fixedLength;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.net.InetSocketAddress;
import java.util.Random;

/**
 * [说明]:用固定长度的帧来解决粘包半包问题。
 * 处理：
 * 对于消息长度不足的情况，需要使用规定的字节填充；
 *
 * @author mini-zch
 * @date 2025/4/8 17:19
 */
public class Client {
    public static void main(String[] args) throws InterruptedException {
        new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {

                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                char c = '0';
                                ByteBuf buf = ctx.alloc().buffer();
                                for (int i = 0; i < 10; i++, c++) {
                                    Random random = new Random();
                                    //fillBytes得到length固定的byte数组
                                    byte[] bytes = fillBytes(c, random.nextInt(10) + 1);
                                    buf.writeBytes(bytes);
                                }
                                ctx.writeAndFlush(buf);
                            }
                        });
                    }
                })
                .connect(new InetSocketAddress("127.0.0.1", 8080))
                .sync();

    }

    /**
     * 将字节c重复i次，然后剩下的用_填充，最终返回一个长度位10的字节数组
     */
    private static byte[] fillBytes(char c, int i) {
        byte[] bytes = new byte[10];
        for (int j = 0; j < 10; j++) {
            if (j < i) {
                bytes[j] = (byte) c;
            } else {
                bytes[j] = (byte) '_';
            }
        }
        return bytes;
    }
}
