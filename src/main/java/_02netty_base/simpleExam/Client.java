package _02netty_base.simpleExam;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

/**
 * @author mini-zch
 * @date 2025/4/3 14:27
 */
@Slf4j
public class Client {
    public static void main(String[] args) throws InterruptedException {
        Logger log = org.slf4j.LoggerFactory.getLogger(Client.class);

        new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new StringEncoder());
                    }
                })
                .connect("127.0.0.1", 8080)
                .sync()
                .channel()
                .writeAndFlush("hello,world");
    }
}
