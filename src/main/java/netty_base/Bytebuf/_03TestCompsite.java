package netty_base.Bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;

import static netty_base.Bytebuf._01TestBytebuf.log;

/**
 * @author mini-zch
 * @date 2025/4/8 14:36
 */
public class _03TestCompsite {
    public static void main(String[] args) {
        ByteBuf buf1 = ByteBufAllocator.DEFAULT.buffer(10);
        ByteBuf buf2 = ByteBufAllocator.DEFAULT.buffer(10);
        buf1.writeBytes(new byte[]{'a', 'b', 'c', 'd', 'e', 'f'});
        buf2.writeBytes(new byte[]{'0', '1', '2', '3', '4', '5'});
        CompositeByteBuf bufs1 = ByteBufAllocator.DEFAULT.compositeBuffer();
        System.out.println(bufs1.refCnt()); //初始创建后的 引用计数 是1
        bufs1.retain();
        System.out.println(buf1.refCnt()); //1
        System.out.println(buf2.refCnt()); //1
        System.out.println(bufs1.refCnt()); //2
        bufs1.addComponents(true,buf1, buf2);
        log(bufs1);

        /*
        * [注意]：下面注释的代码的逻辑：先释放buf1，然后读取bufs1中的第6个、第3个位置的字节。
        *   结果：
        *       ①第6个位置的字节正常读取，因为buf2没有被release
        *       ②第3个位置的字节报错，因为buf1被release了
        *       ③因为bufs1中包含buf1的片段，buf1被释放了，因此log(bufs1)也会报错
        *   结论：compositeByteBuf是多个ByteBuf的组合。。在组合后需要分别使用各个Bytebuf的retain()增加每一个Bytebuf的引用
        * 计数（防止其中的一个或者多个Bytebuf被释放）；在使用结束后需要调用各个Bytebuf的release()方法对每一个Bytebuf释放
        * */
//        buf1.release();
//        System.out.println(bufs1.getByte(6)); //正常读取
//        System.out.println(bufs1.getByte(3)); //报错
//        log(bufs1); //报错

        /*
        * [说明]：
        *   1. 合并后的缓冲区的capacity会根据合并后的缓冲区的容量自动调整，并不是所有缓冲区capacity之和...比如：
        * 这里合并后的bufs1的capacity是12,因为buf1 和 buf2 都只有6个字节
        *   2. 多个缓冲区composite之后 原始的缓冲区还是可以继续写入的，见下面的代码。但是写入后之前合并的缓冲区数据
        * 不变
        *   3. buf1和buf2重新写入数据后合并到一个compositeByteBuf中，发现新的bufs2的容量是14，数据也改变了。因此
        * 每一次合并的compositeByteBuf都是独立的————不会受到原始缓冲区后续变化的影响。相当于那些个原始缓冲区的快照
        * */
        buf1.writeByte('g');
        buf2.writeByte('6');
        log(buf1); //buf1的数据改变，多了一个字节‘g’
        log(buf2); //buf2的数据改变，多了一个字节‘6’
        log(bufs1); //bufs1的数据、容量都没有改变

        //重新将写入数据后的buf1，buf2合并到一个compositeByteBuf中,发现新的bufs2的容量是14，数据也改变了
        CompositeByteBuf bufs2 = ByteBufAllocator.DEFAULT.compositeBuffer();
        bufs2.addComponents(true,buf1, buf2);
        log(bufs2);
    }
}
