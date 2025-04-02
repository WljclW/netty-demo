package c4;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;

import static c4.ByteBufferUtil.debugRead;

/**
 * @author mini-zch
 * @date 2025/4/1 15:18
 */
/**
 * 1.Selector selector = Selector.open()。拿到一个Selector实例，内部的实现依然是根据系统的不同的返回合适的Selector.
 * 2.向Selector注册的通道必须是非阻塞模式的————服务端 以及 跟客户端的连接通道 都需要使用configureBlocking配置为非阻塞模式，
 *      否则，在调用register方法注册的时候会报异常：java.lang.IllegalBlockingModeException
 * 3.向Selector注册通道的时候，jvm提供了一个AbstractSelectableChannel————所有的selectable channel的基类。管理着通道的
 *      注册、注销、关闭等操作。
 * 4.通过Selector.selectedKeys()
 * =============================================================================================
 * Selector类的源码注释：
 * 1.可以通过调用“Selector的open()”，也可以通过调用“ custom selector provider的openSelector()”，Selector将保持开启直
 *      到调用close()方法
 * 2.一个SelectionKey对象代表某个通道在Selector的注册。并且Selector维护着关于这种对象的三个集合：
 *      ①keys()。返回有多少个通道注册到了这个Selector
 *      ②selectedKeys()————keys()的子集。这个集合中的元素是对应通道有就绪的事件，需要处理
 *      ③canceledKeys()————keys()的子集。这个集合中元素对应已经取消，但是元素对应的通道还没有取消注册的SelectionKey
 * 3.通过channel的register方法注册到Selector的时候，会将对应的selectionKey放入到keys中；
 *   通过关闭channel或者调用cancel()，会将对应的selectionKey放入到canceledKeys中；取消一个key将会导致在下一个selection
 *      操作期间这个key不再有效，并会从所有的集合中移除。
 *   这些key可以通过调用集合的remove()或者迭代器的remove()方法进行删除。不允许直接向上述的集合中通过add方法手动添加selectionKey
 *   调用clear()时，维护的三个集合都会清空
 *   ============================selection
 * 1.可以通过select()/selectNow()/select(Long)来获取已经就绪(等待IO)的事件。。是不是阻塞等待有通道就绪事件发生、阻塞等待多久？是
 *      这三个方法之间的区别
 *   ============================concurrent
 * 1. 维护的三个集合中，keys()是线程安全的；其他的两个selectedKeys()和canceledKeys()是线程不安全的
 * 2. 当某一个key的事件正在被selector处理时，对于该selectionKey感兴趣的事件的修改不会影响当前的处理，会影响后面对这个selectionKey的影响
 * */
public class ServerSelector {
    public static void main(String[] args) throws IOException {
        org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ServerSelector.class);
        /*1.创建Selector，用来管理多个channel*/
        Selector selector = Selector.open();
        /*2.创建服务端的通道*/
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false); //在Selector中注册的通道必须是非阻塞模式的
        ssc.bind(new InetSocketAddress(8080));
        /*3.将通道注册到Selector*/
        SelectionKey sscKey = ssc.register(selector, 0, null);
        /*4.注册自己感兴趣的事件*/
        sscKey.interestOps(SelectionKey.OP_ACCEPT);
        while(true){
            /*5.selector在没有事件发生的时候阻塞，有事件发生时恢复执行*/
            log.debug("selector阻塞中.......");
            int select = selector.select();
//            int select = selector.selectNow(); //表示不阻塞，执行到时即使没有就绪的事件也立马返回
//            int select = selector.select(3000); //阻塞等待3000毫秒就不等了，直接返回并继续向后执行
            log.debug("selector执行了.......{}",select);
            log.debug("selectionKeys的大小.......{}",selector.selectedKeys().size());
            log.debug("selectionKeys的大小.......{}",selector.keys().size());
            /*6.处理事件。selectedKeys包含所有发生的事件*/
            /*这里对于发生事件的selectionKey遍历时需要使用 迭代器，因为在遍历的过程中涉及删除操作。*/
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while(iterator.hasNext()){
                log.debug("遍历所有的selectionKey....");
                SelectionKey selectionKey = iterator.next();
                iterator.remove();
//                selectionKey.cancel(); //事件发生后要麽处理、要麽取消，不能置之不理
                log.debug("selectKey.......{}",selectionKey);
                if(selectionKey.isAcceptable()){
                    //对于accept事件，selectionKey对应的channel是ServerSocketChannel因为是服务端的
                    ServerSocketChannel serverChannel = (ServerSocketChannel)selectionKey.channel();
                    /*⚠注意：1. 只要accept成功就立马需要设置非阻塞模式，否则注册时候会报异常;
                    *       2. 每一次处理完一个通道的事件后，需要手动删除对应的SelectionKey，否则会出现 空指针异常*/
                    SocketChannel sc = serverChannel.accept();
                    log.debug("连接成功......{}",sc);
                    sc.configureBlocking(false);
                    //以后sc这个SocketChannel的事件就由scKey来管理
                    SelectionKey scKey = sc.register(selector, 0, null);
                    scKey.interestOps(SelectionKey.OP_READ);
//                    sc.configureBlocking(false); //这里出错，因为register方法在上两行已经调用，调用的时候通道sc还是阻塞模式，因此会报错
                    log.debug("connected....{}",sc);
                }else if(selectionKey.isReadable()){
                    /*对于读事件:首先拿到出发事件对应的channel；调用read方法读取数据到ByteBuffer*/
                    try{
                        SocketChannel sc = (SocketChannel) selectionKey.channel();
                        log.debug("read....{}",sc);
                        ByteBuffer buffer = ByteBuffer.allocate(16);
                        int read = sc.read(buffer);
                        //如果是异常读(比如客户端关闭等)，read方法会返回-1，此时需要把key取消(从selectedKeys中删除)
//                        if(read==-1){
//                            selectionKey.cancel();
//                        }else{ //read返回值不是-1，说明正常读取到数据
                            buffer.flip();
                            debugRead(buffer);
                        System.out.println(Charset.defaultCharset().decode(buffer));
                        log.debug("deg....");
//                        }
                    }catch (IOException e){
                        e.printStackTrace();
                        //出现异常时(比如连接断开等)，需要将key取消(从selectedKeys中删除)
                        selectionKey.cancel();
                    }
                }
            }
        }


    }
}
