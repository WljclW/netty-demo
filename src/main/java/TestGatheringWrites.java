import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * 将多个ByteBuffer的内容写入到FileChannel
 * */
public class TestGatheringWrites {
    public static void main(String[] args) {
        ByteBuffer buffer1 = StandardCharsets.UTF_8.encode("hello");
        ByteBuffer buffer2 = StandardCharsets.UTF_8.encode("world");
        ByteBuffer buffer3 = StandardCharsets.UTF_8.encode("你好");

        try (RandomAccessFile channel = new RandomAccessFile("words2.txt", "rw")) {
            channel.getChannel().write(new ByteBuffer[]{buffer1, buffer2, buffer3});
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
