import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 测试将一个文件的内容，分散写入到多个ByteBuffer
 * */
public class TestScatteringReads {
    public static void main(String[] args) {
        try {
            FileChannel channel = new RandomAccessFile("words.txt", "r").getChannel();
            /*将channel中的数据分别写入到三个ByteBuffer*/
            ByteBuffer b1 = ByteBuffer.allocate(3);
            ByteBuffer b2 = ByteBuffer.allocate(3);
            ByteBuffer b3 = ByteBuffer.allocate(3);
            channel.read(new ByteBuffer[]{b1,b2,b3}); //read方法可以接受一个ByteBuffer数组
            /*写完之后需要将三个ByteBuffer切换为读模式。这样的话position才会置为0*/
            b1.flip();
            b2.flip();
            b3.flip();
            ByteBufferUtil.debugAll(b1);
            ByteBufferUtil.debugAll(b2);
            ByteBufferUtil.debugAll(b3);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
