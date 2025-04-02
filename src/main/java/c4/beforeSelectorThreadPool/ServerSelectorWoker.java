package c4.beforeSelectorThreadPool;

/**
 * @author mini-zch
 * @date 2025/4/2 14:08
 */

import c4.ByteBufferUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import static c4.ByteBufferUtil.debugRead;

/**
 * []:用不同的线程分别处理连接、处理IO读写....不成功的实现
 * 【】：
 *  1.重点关注“worker.register()”出现的地方有什么区别？
 *  2.为什么这两处都不合理？
 *  3.最关键的点是"channel注册到worker.selector 和 worker.selector调用select()"的先后顺序*/
public class ServerSelectorWoker {
    public static void main(String[] args) throws IOException {
        Thread.currentThread().setName("Boss thread==================");
        Selector boss = Selector.open();
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.bind(new InetSocketAddress(8080));
        ssc.register(boss, SelectionKey.OP_ACCEPT, null);
        Worker worker = new Worker("worker-1");
        /**
         * 第一处：
         * 42.worker.register();写在这里有问题.原因：
         *      一旦执行register方法，就会开始线程，导致在Worker的run中while(true)循环中执行“selector.select()”
         *   但是又没有事件到来(其实初始时它里面都没有注册的channel呢...)，因此Selector就会阻塞到这里。。。⚠在
         *   Selector阻塞的期间，调用Channel的register方法 注册到Selector时就会被阻塞住。
         * */
//        worker.register();
        while(true){
            boss.select();
            Iterator<SelectionKey> iterator = boss.selectedKeys().iterator();
            while(iterator.hasNext()){
                SelectionKey sc = iterator.next();
                /*注意每一次处理时间后需要删除已处理的SelectionKey;
                * [注意]如果还没有执行过next() 或者 上次执行完next()方法后已经执行过remove()就会导致IllegalStateException异常*/
                iterator.remove();
                if(sc.isAcceptable()){
                    SocketChannel channel = ssc.accept();
                    channel.configureBlocking(false);
                    System.out.println("connected.....");
                    System.out.println("before register成功.....");
                    /**
                     * 第二处：
                     *    放在这里，可以保证一个线程的读操作能被处理————最本质的逻辑是因为channel注册到
                     *  worker.selector中后，在新的线程中才执行了selector.select()。
                     *    仅仅是因为两个线程中这两个步骤(channel注册到selector 和 selector.select())的
                     *  先后顺序符合要求，因此最终导致能成功处理客户端的写事件。
                     *    但是如果此时一个新的客户端连接服务端，依然可以连接成功（假设建立的通道是channel），
                     *  因为负责连接的selector是boss线程的selector。而当它连接成功想注册到worker.selector中
                     *  时，就会发现注册不了，因为worker中的selector在处理完读事件后，执行select方法时会被阻
                     *  塞住，直到下一次出现感兴趣的事件时恢复执行，channel才能成功的注册到worker.selector中。
                     * */
                    worker.register();
                    /*与客户端建立的通道需要注册到worker的selector!!*/
                    channel.register(worker.selector, SelectionKey.OP_READ, null);
                    System.out.println("after register成功.....");
                    worker.selector.wakeup();
                }
            }
        }
    }

    static class Worker implements Runnable {
        private Thread thread;
        private Selector selector;
        private String name;
        private volatile AtomicBoolean started = new AtomicBoolean(false);

        public Worker(String name){
            this.name = name;
        }

        /*初始化线程 和 selector*/
        public void register() throws IOException {
            if(started.compareAndSet(false, true)){
                selector = Selector.open();
                thread = new Thread(this, name);
                thread.start();
            }
        }


        @Override
        public void run() {
            while(true){
                try {
                    System.out.println("worker线程开始工作,select执行之前.....");
                    selector.select();
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    System.out.println("worker线程开始工作,select执行之后.....");
                    System.out.println(selector.keys());
                    while(iterator.hasNext()){
                        System.out.println("进入到遍历");
                        SelectionKey sk = iterator.next();
                        iterator.remove();
                        if(sk.isReadable()){
                            System.out.println("进入到读时间的处理");
                            ByteBuffer buffer = ByteBuffer.allocate(16);
                            SocketChannel channel = (SocketChannel) sk.channel();
                            System.out.println("before read...");
                            channel.read(buffer);
                            System.out.println("after read...");
                            buffer.flip();
                            ByteBufferUtil.debugAll(buffer);
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
