package netty_partialAndMerging_Packet.encode_decode;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * []：
 * LengthFieldBasedFrameDecoder构造器中的几个参数解释：
 *  1.lengthFieldOffset："长度字段"的偏移量。即从头跳过几个字节能拿到表示长度字段
 *  2.lengthFieldLength："长度字段"占用几个字节。即用几个字节来表示长度
 *  3.lengthAdjustment：读取完"长度字段"后，还需要跳过几个字节后，才是真正内容的开始
 *  4.initialBytesToStrip：跳过前面的几个字节，作为解码后的内容*/
public class TestLengthFieldBasedFrameDecoder {
    public static void main(String[] args) {
        EmbeddedChannel channel = new EmbeddedChannel(
                new LengthFieldBasedFrameDecoder(
                        1024,
                        0,
                        4,
                        0,
                        4
                ),
                new LoggingHandler(LogLevel.DEBUG)
        );

        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        send(buf, "hello, world");
        send(buf, "Hi!");
        /*
        * 下面的两个调用的区别是什么？？为什么打印的结果不相同
        * */
        channel.writeInbound(buf); //1
//        channel.writeAndFlush(buf); //2
    }

    private static void send(ByteBuf buf, String s) {
        byte[] bytes = s.getBytes();
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }
}
