import java.nio.ByteBuffer;

/**
 * 测试ByteBuffer的读写方法：
 *      写：put；
 *      读：get;
 *      模式切换：flip——写模式切换为读模式；
 *               compact——读模式切换为写模式，并且将没有读的数据移动到缓冲区的开始位置
 *
 * */
public class ByteBufferReadWriter {
    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put((byte) 0x61);
        ByteBufferUtil.debugAll(buffer);
        buffer.put(new byte[]{0x62, 0x63,0x64} );
        ByteBufferUtil.debugAll(buffer);
//        System.out.println(buffer.get()); //没有切换模式读不到有用的数据
        buffer.flip(); //切换为读模式
        System.out.println((char) buffer.get()); //会读取到缓冲区的第一个字节A
        ByteBufferUtil.debugAll(buffer);
        buffer.compact(); //切换为写模式，compact会移动数据，因此缓冲区的第一个字节现在是98——字符b
        ByteBufferUtil.debugAll(buffer);
        buffer.put(new byte[]{0x65, 0x66, 0x67});
        ByteBufferUtil.debugAll(buffer);
    }
}
