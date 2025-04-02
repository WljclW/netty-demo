package c4.selectThreadPool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
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
            workers[i].register();
        }
        AtomicInteger atomicInteger = new AtomicInteger();
        while(true){
            boss.select();
            Iterator<SelectionKey> iterator = boss.selectedKeys().iterator();
            while(iterator.hasNext()){
                SelectionKey key = iterator.next();
                iterator.remove();
                if(key.isAcceptable()){
                    SocketChannel channel = ssc.accept();
                    channel.configureBlocking(false);
                    channel.register(workers[atomicInteger.getAndIncrement()%workers.length].selector, SelectionKey.OP_READ,null);
                }
            }
        }
    }

    static class Worker implements Runnable{
        private String name;
        private Selector selector;
        private Thread thread;

        public Worker(String name){
            this.name = name;
        }

        public void register() throws IOException {
            selector = Selector.open();
            thread = new Thread(this, name);
            thread.start();
        }

        @Override
        public void run() {
            while(true){
                try {
                    selector.select();
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
