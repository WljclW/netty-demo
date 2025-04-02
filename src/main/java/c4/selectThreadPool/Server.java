package c4.selectThreadPool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static c4.ByteBufferUtil.debugRead;

/**
 * @author mini-zch
 * @date 2025/4/2 20:00
 */
public class Server {
    public static void main(String[] args) throws IOException {
        Selector boss = Selector.open();
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.bind(new InetSocketAddress(8080));
        SelectionKey sk = ssc.register(boss, SelectionKey.OP_ACCEPT, null);
        Worker[] workers = new Worker[2];
        for (int i = 0; i < 2; i++) {
            workers[i] = new Worker("worker-" + i);
        }
        AtomicInteger index = new AtomicInteger();
        while(true){
            boss.select();
            Iterator<SelectionKey> iterator = boss.selectedKeys().iterator();
            while(iterator.hasNext()){
                SelectionKey key = iterator.next();
                iterator.remove();
                if(key.isAcceptable()){
                    SocketChannel channel = ssc.accept();
                    channel.configureBlocking(false);
                    //round robin算法
                    workers[index.getAndIncrement() % workers.length].register(channel);
                }
            }
        }
    }

    static class Worker implements Runnable{
        private String name;
        private Selector selector;
        private Thread thread;
        private volatile AtomicBoolean started = new AtomicBoolean(false);
        private ConcurrentLinkedDeque<Runnable> queue = new ConcurrentLinkedDeque<Runnable>();

        public Worker(String name){
            this.name = name;
        }

        public void register(SocketChannel channel) throws IOException {
            if(started.compareAndSet(false, true)){
                selector = Selector.open();
                thread = new Thread(this, name);
                thread.start();
            }
            queue.add(() -> {
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
                    selector.select();
                    Runnable poll = queue.poll();
                    if(poll!=null){
                        poll.run();
                    }
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while(iterator.hasNext()){
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        if(key.isReadable()){
                            SocketChannel channel = (SocketChannel) key.channel();
                            ByteBuffer buffer = ByteBuffer.allocate(16);
                            channel.read(buffer);
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
