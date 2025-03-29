import java.nio.ByteBuffer;

public class TestByteBufferExam {
    /**
     * 网络上有多条数据发送给服务端，数据之间使用 \n 进行分隔
     * 但由于某种原因这些数据在接收时，被进行了重新组合，例如原始数据有3条为
     *
     * * Hello,world\n
     * * I'm zhangsan\n
     * * How are you?\n
     *
     * 变成了下面的两个 byteBuffer (黏包，半包)
     *
     * * Hello,world\nI'm zhangsan\nHo
     * * w are you?\n
     *
     * 现在要求你编写程序，将错乱的数据恢复成原始的按 \n 分隔的数据
     * */
    public static void main(String[] args) {
        ByteBuffer source = ByteBuffer.allocate(32);
        source.put("Hello,world\nI'm zhangsan\nHo".getBytes());
        split(source);
        source.put("w are you?\n".getBytes());
        split(source);
    }

    private static void split(ByteBuffer source) {
        source.flip();
        /*遍历source的每一个字节*/
        for (int i = 0; i < source.limit(); i++){
            /*每次遇到'\n'就表示找到了一个完整的消息*/
            if(source.get(i)=='\n'){
                /*计算这个消息的长度*/
                int length = i+1-source.position();
                ByteBuffer buffer = ByteBuffer.allocate(length);
                /*这里的循环j仅仅表示一次拿到这个完整消息的每一个字符*/
                for (int j = 0; j < length; j++){
                    buffer.put(source.get());
                }
                ByteBufferUtil.debugAll(buffer);
            }
        }

        source.compact();
    }
}
