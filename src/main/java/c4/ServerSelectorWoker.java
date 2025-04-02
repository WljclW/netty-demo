package c4;

/**
 * @author mini-zch
 * @date 2025/4/2 14:08
 */

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import static c4.ByteBufferUtil.debugRead;

/**用不同的线程分别处理连接、处理IO读写*/
public class ServerSelectorWoker {
    public static void main(String[] args) throws IOException {
        Thread.currentThread().setName("Boss thread==================");
        Selector boss = Selector.open();
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.bind(new InetSocketAddress(8080));
        ssc.register(boss, SelectionKey.OP_ACCEPT, null);
        Worker worker = new Worker("worker-1");
        worker.register();
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
                    System.out.println("accept成功.....");
                    channel.register(worker.selector, SelectionKey.OP_READ, null);
                    System.out.println("register成功.....");
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
                            debugRead(buffer);
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
