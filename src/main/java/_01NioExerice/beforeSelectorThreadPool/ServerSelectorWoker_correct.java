package _01NioExerice.beforeSelectorThreadPool;

/**
 * @author mini-zch
 * @date 2025/4/2 14:08
 */

import _01NioExerice.ByteBufferUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * []:用不同的线程分别处理连接、处理IO读写....不成功的实现
 * 【】：
 *  1.重点关注“worker.register()”出现的地方有什么区别？
 *  2.为什么这两处都不合理？
 *  3.最关键的点是"channel注册到worker.selector 和 worker.selector调用select()"的先后顺序*/
public class ServerSelectorWoker_correct {
    public static void main(String[] args) throws IOException {
        Thread.currentThread().setName("Boss thread==================");
        Selector boss = Selector.open();
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.bind(new InetSocketAddress(8080));
        ssc.register(boss, SelectionKey.OP_ACCEPT, null);
        Worker worker = new Worker("worker-1");
        while(true){
            boss.select();
            Iterator<SelectionKey> iterator = boss.selectedKeys().iterator();
            while(iterator.hasNext()){
                SelectionKey sc = iterator.next();
                iterator.remove();
                if(sc.isAcceptable()){
                    SocketChannel channel = ssc.accept();
                    channel.configureBlocking(false);
                    /**
                     * [关键信息]
                     *    这里调用worker.register(希望通过在register方法中将形参的channel注册到worker的selector)，但是
                     * 当前处于的是Boss线程。而worker的selector在select方法的地方是在worker-1线程，因此这两个步骤的先后
                     * 顺序还是未知的。
                     * */
                    worker.register(channel);
                }
            }
        }
    }

    static class Worker implements Runnable {
        private Thread thread;
        private Selector selector;
        private String name;
        private volatile AtomicBoolean started = new AtomicBoolean(false);
        private ConcurrentLinkedDeque<Runnable> queue =new ConcurrentLinkedDeque<Runnable>();


        public Worker(String name){
            this.name = name;
        }

        /*初始化线程 和 selector*/
        public void register(SocketChannel channel) throws IOException {
            if(started.compareAndSet(false, true)){
                selector = Selector.open();
                thread = new Thread(this, name);
                thread.start();
            }
            /**
             * 向队列中添加任务，但是并没有立刻执行。
             * 这里是在哪个线程执行的呢？？Boss，因为在Boss线程(即主线程)的while中调用了worker的register方
             *    法。即在主线程中将我们想要做的事存到阻塞队列中，但是并没有立刻执行。。。然后在其他的线程中
             *    合适的时机从队列中拿出任务执行(即实现了：我们让该线程在特定的时机去执行特定的代码)
             * */
            queue.add(()->{
                try {
                    channel.register(selector, SelectionKey.OP_READ, null);
                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                }
            });
            selector.wakeup();
        }


        @Override
        public void run() {
            while(true){
                try {
                    System.out.println("worker线程开始工作,select执行之前.....");
                    selector.select();
                    Runnable poll = queue.poll();
                    if(poll!=null){
                        poll.run();
                    }
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
