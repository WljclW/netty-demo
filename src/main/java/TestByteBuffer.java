import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/*ctrl+p：光标在括号中查看方法参数*/
@Slf4j
public class TestByteBuffer {
    public static void main(String[] args) {
        //FileChannel
//        try {
//            /*step1：想读取文件，先拿到一个FileChannel.默认是从项目的根目录下找*/
//            FileChannel channel = new FileInputStream("data.txt").getChannel();
//            /*step2:拿到一段buffer*/
//            ByteBuffer buffer = ByteBuffer.allocate(10);
//            /*step3：从channel中读取数据，写入到buffer*/
//            channel.read(buffer);
//            //下面进行读取操作。原生的ByteBuffer在读取之前需要先转为读模式
//            /*step4：调用flip切换为读模式*/
//            buffer.flip();
//            /*step5：只要缓冲区还有数据，就拿出并打印*/
//            while(buffer.hasRemaining()){
//                byte b = buffer.get();
//                System.out.println((char)b);
//            }
//        }catch (Exception e){
//            System.out.println(e.getMessage());
//            System.out.println("异常....");
//        }


        /**上面的写法，只会打印出前十个字节。如果想要读取完整的文件内容，可以采用下面的方法*/
        try{
            //1.得到FileChannel
            FileChannel channel = new FileInputStream("data.txt").getChannel();
            //2.分配一个指定大小的缓冲区
            ByteBuffer buffer = ByteBuffer.allocate(10);
            while(true){
                //3.将channel中的数据读到buffer中
                int read = channel.read(buffer);
                log.debug("读取到的字节数:{}",read);
                //4.判断是否读完。read方法返回-1表示读不到新的数据了
                if(read == -1){
                    break;
                }
                //5.将buffer从写模式切换到读模式
                buffer.flip();
                while(buffer.hasRemaining()){
                    byte b = buffer.get();
                    System.out.println((char)b);
                }
                //6.读完了需要将读模式重新切换为写模式
                buffer.clear();
            }
        }catch (Exception e){

        }


    }
}
