import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;

/**
 * ByteBuffer和字符串之间的转换方法
 * */
public class TestByteBufferString {
    public static void main(String[] args) {
         //1.字符串转换为ByteBuffer
        ByteBuffer buffer1 = ByteBuffer.allocate(16);
        buffer1.put("hello".getBytes());
        ByteBufferUtil.debugAll(buffer1);

        //2.Charsets的encode方法。将字符串按照编码格式进行编码。
        ByteBuffer buffer2 = StandardCharsets.UTF_8.encode("hello");
        ByteBufferUtil.debugAll(buffer2);

        //3.wrap
        ByteBuffer buffer3 = ByteBuffer.wrap("hello".getBytes());
        ByteBufferUtil.debugAll(buffer3);


        //将buffer中的内容解码转换为字符串
        CharBuffer decode_buffer2 = StandardCharsets.UTF_8.decode(buffer2);
        System.out.println(decode_buffer2.toString());
        System.out.println(decode_buffer2);

        /*由于put方法操作后还是写指针，因此直接解码时是有问题的。
        put方法后得到的ByteBuffer正确的解码方式：
            先调用flip转换为读模式，再解码*/
        buffer1.flip();
        CharBuffer decode_buffer1 = StandardCharsets.UTF_8.decode(buffer1);
        System.out.println(decode_buffer1);

    }
}
