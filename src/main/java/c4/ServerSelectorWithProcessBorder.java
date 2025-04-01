package c4;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;

import static c4.ByteBufferUtil.debugRead;

/**
 * ServerSelector的问题是：消息的最大长度只能是16，超出16字节的话会导致前面的16倍数
 *      的字节消息丢失。
 * 这里改进————使用特殊标记来标记消息的结尾
 * */
@Slf4j
public class ServerSelectorWithProcessBorder {
    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.bind(new InetSocketAddress(8080));
        SelectionKey selectionKey = ssc.register(selector, 0, null);
        selectionKey.interestOps(SelectionKey.OP_ACCEPT);
        while (true){
            selector.select();
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while(iterator.hasNext()){
                SelectionKey key = iterator.next();
                iterator.remove();
                if(key.isAcceptable()){
                    log.debug("accept...");
                    ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                    SocketChannel sc = serverChannel.accept();
                    sc.configureBlocking(false);
                    /*将一个ByteBuffer和某一个selectionKey关联起来*/
                    ByteBuffer buffer = ByteBuffer.allocate(16);
                    SelectionKey sk = sc.register(selector, 0, buffer);
                    sk.interestOps(SelectionKey.OP_READ);
                }else if(key.isReadable()){
                    log.debug("read...");
                    try{
                        SocketChannel channel = (SocketChannel) key.channel();
                        ByteBuffer buffer = (ByteBuffer) key.attachment();
//                        ByteBuffer buffer = ByteBuffer.allocate(16);
                        int read = channel.read(buffer);
                        if(read==-1){
                            key.cancel();
                        }else{
                            split(buffer);
                            if(buffer.position()==buffer.limit()){
                                ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity()*2);
                                buffer.flip();
                                newBuffer.put(buffer);
                                key.attach(newBuffer);
                            }
                            debugRead(buffer);
                            System.out.println(Charset.defaultCharset().decode(buffer));
                        }
                    }catch (IOException e){
                        e.printStackTrace();
                        key.cancel();
                    }
                }
            }
        }
    }


    private static void split(ByteBuffer source) {
        source.flip();
        for (int i=0;i<source.limit();i++){
            if(source.get(i)=='\n'){
                int length = i+1-source.position();
                ByteBuffer buffer = ByteBuffer.allocate(length);
                for (int j=0;j<length;j++){
                    buffer.put(source.get()); //get方法会自动增加position
                }
                ByteBufferUtil.debugAll(buffer);
            }
        }
        source.compact();
    }



}
