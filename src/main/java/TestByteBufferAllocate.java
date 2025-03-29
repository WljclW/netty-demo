import java.nio.ByteBuffer;

public class TestByteBufferAllocate {
    public static void main(String[] args) {
        /*ByteBuffer.allocate分配到的内存是在堆内存，对应的类：HeapByteBuffer
        * HeapByteBuffer：虚拟机中的堆内存。读写效率较低，涉及到GC的影响，因此这样的缓冲器
        *   可能会在GC时涉及到移动、复制*/
        ByteBuffer buffer = ByteBuffer.allocate(16);
        System.out.println(buffer.getClass());
        System.out.println(buffer.getClass().getSuperclass());
        /*ByteBuffer.allocateDirect分配到的内存是在直接内存，对应的类是：DirectByteBuffer
        * DirectByteBuffer：直接内存。读写效率高(少一次数据的拷贝)，不受GC影响；分配的效率
        *   低一些；可能会导致内存泄露(需要手动释放)*/
        ByteBuffer buffer1 = ByteBuffer.allocateDirect(16);
        System.out.println(buffer1.getClass());
        System.out.println(buffer1.getClass().getSuperclass());
    }
}
