import java.nio.ByteBuffer;

/**
 * byteBuffer的一些读取的方法
 * */
public class TestByteBufferRead {
    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put(new byte[]{'a','b','c','d'});
        buffer.flip();

/*        //rewind：从头开始读
        buffer.get(new byte[4]); //读取4个字节，此时理论上读指针已经在索引为4的位置
        ByteBufferUtil.debugAll(buffer); //position=4表明，读指针在索引为4的位置
        buffer.rewind(); //读指针回到索引为0的位置
        ByteBufferUtil.debugAll(buffer); //再次打印发现position重置为0，limit的值没变
        System.out.println((char) buffer.get());
*/

        /*mark和reset
        * mark:用于在ByteBuffer的某个位置做标记；
        * reset:将position重置到merk的位置
        * 场景：比如某一段数据需要反复读*/
/*        byte b = buffer.get();
        System.out.println((char) b);
        byte b1 = buffer.get();
        System.out.println((char) b1);
        buffer.mark();  //mark()方法标记一下索引位2的位置
        System.out.println((char) buffer.get());
        System.out.println((char) buffer.get());
        buffer.reset();  //上面两行通过get分别访问索引2、3的位置，因此这里理论读指针在4的位置，reset()方法可以把读指针重置到mark()的位置
        //因为经过reset()后读指针重新回到mark()方法标记的地方即索引为2的位置
        System.out.println((char) buffer.get());
        System.out.println((char) buffer.get());
*/

        /*
        * get(index)：获取指定索引位置的字节。并且读指针的位置不变
        * */
        byte b = buffer.get(2);
        System.out.println((char)b);
        ByteBufferUtil.debugAll(buffer);
    }
}
