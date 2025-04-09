package _04chat.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.nio.ByteOrder;

/**
 * 根据自己的协议封装一个解码帧模样具。
 * netty内部的类LengthFieldBasedFrameDecoder是没有无参构造器的
 * */
public class ProcotolFrameDecoder extends LengthFieldBasedFrameDecoder {
    public ProcotolFrameDecoder() {
        this(1024, 12, 4, 0, 0);
    }

    public ProcotolFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }
}
