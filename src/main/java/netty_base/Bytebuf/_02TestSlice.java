package netty_base.Bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import static netty_base.Bytebuf._01TestBytebuf.log;

/**
 * [slice]
 *      1. slice方法会返回原始缓冲区Bytebuf的子缓冲区，子缓冲区与原始缓冲区共享内存，修改时会互相影响。
 *      2. slice方法后得到的子缓冲区有 和 原始缓冲区 独立的writeIndex、readIndex和markIndex
 * @author mini-zch
 * @date 2025/4/8 10:04
 */
public class _02TestSlice {
    public static void main(String[] args) {
//        correctUse(); //测试正确使用例子
//        System.out.println("=======================");

        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(10);
        buf.writeBytes(new byte[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j'});
        log(buf);


        buf.slice(); //返回该缓冲区所有可读字节的切片
        ByteBuf slice1 = buf.slice(0, 5); //返回buf缓冲区从索引0开始，长度为5的切片
        ByteBuf slice2 = buf.slice(5, 5);
        System.out.println(slice1.refCnt());
        System.out.println(buf.refCnt());
        //建议在切片之后使用retain()，避免底层缓冲区被释放，从而导致切片后续使用时报错
        slice1.retain();
        System.out.println(slice1.refCnt());
        System.out.println(buf.refCnt());
        buf.release();
        System.out.println(slice1.refCnt());

        /*[注意]slice之后，所有的切片都不能写入，最后一个切片也不例外。。会报异常：java.lang.IndexOutOfBoundsException*/
//        slice2.writeByte('k');

        /*如果原始的buf被释放了，则slice()后的所有切片的相关操作都会报错：io.netty.util.IllegalReferenceCountException*/
//        buf.release();
//        System.out.println(slice1.readByte());

        /*因此，发生切片时建议的做法是：有slice切片来做最后的release()*/
//        System.out.println(slice1.readByte()); //挨个读取并移动读指针
//        System.out.println(slice1.readByte());
//        System.out.println(slice2.getByte(4));
//        System.out.println(slice2.getByte(5)); //slice2的长度是5，没有索引为5的位置，报错：java.lang.IndexOutOfBoundsException
//
//        System.out.println(buf.getByte(8)); //slice1被释放了，读取buf索引为8位置的字节也会报错，为什么？？

        /**/
        System.out.println(slice1.readByte()); //挨个读取并移动读指针
        System.out.println(slice1.readByte());
        slice1.release();
        slice2.release(); //上面有slice1.release()，这里会报异常：io.netty.util.IllegalReferenceCountException
        System.out.println(slice2.getByte(4)); //slice1被释放了，读取slice2时也会报错，为什么？？

        System.out.println(buf.getByte(8)); //slice1被释放了，读取buf索引为8位置的字节也会报错，为什么？？
    }


    /**
     * 下面是正确的使用示例。————每次slice()之后都调用retain()防止引用计数误归零
     * */
    public static void correctUse() {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(10);
        buf.writeBytes(new byte[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j'});
        /*每一次slice()之后都调用retain(),slice()之后单独负责release*/
        ByteBuf slice1 = buf.slice(0, 5);
        slice1.retain();
        ByteBuf slice2 = buf.slice(5, 5);
        slice2.retain();
        System.out.println(slice1.getByte(3));
        slice1.release(); //slice1释放之后，由于slice2调用retain()，因此buf的引用计数还不是0
        /*
        * [说明]即使slice1释放了，但是buf的引用并不是0。。因此下面的buf. 和 slice2. 都是没有问题的，甚至slice1.也没问题！！！
        * */
        System.out.println(slice1.getByte(3));
        System.out.println(buf.getByte(2));
        System.out.println(slice2.getByte(3));

    }
}
