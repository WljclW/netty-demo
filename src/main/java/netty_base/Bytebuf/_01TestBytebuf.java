package netty_base.Bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import static io.netty.buffer.ByteBufUtil.appendPrettyHexDump;
import static io.netty.util.internal.StringUtil.NEWLINE;

public class _01TestBytebuf {
    public static void main(String[] args) {
        /*buffer():默认的容量是256字节；默认分配的类型是 池化的直接内存。。
        * 源码参见ByteBufUtil类的static块*/
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        /*heapBuffer():分配一个 池化的堆内存*/
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.heapBuffer();
        log(buf);
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<8;i++){
            sb.append(i);
        }
        buf.writeBytes(sb.toString().getBytes());
        log(buf);
    }

    private static void log(ByteBuf byteBuf){
        int length = byteBuf.readableBytes();
        int rows = length / 16 + (length % 15 == 0 ? 0 : 1)+4;
        StringBuilder buf = new StringBuilder(rows * 80 * 2)
                .append("read index:").append(byteBuf.readableBytes())
                .append(" write index:").append(byteBuf.writableBytes())
                .append(" capacity:").append(byteBuf.capacity())
                .append(NEWLINE);
        appendPrettyHexDump(buf, byteBuf);
        System.out.println(buf.toString());
    }
}
