package netty_base.future_promise;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

@Slf4j
public class TestNettyFuture {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Logger log = LoggerFactory.getLogger(TestNettyFuture.class);

        /*1. 拿到NIO事件循环组的一个，代表着一个线程*/
        NioEventLoopGroup group = new NioEventLoopGroup();
        EventLoop eventLoop = group.next();

        /*2. 这里的返回值是netty的Future类型。netty的Future是继承自jdk的Future的*/
        Future<Integer> future = eventLoop.submit(new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                log.debug("ecentLoop执行计算。。。");
                Thread.sleep(3000);
                return 88;
            }
        });

        /*3.1 可以使用jdk类似的方式来同步等待获取结果..
        *   这种方式获取结果的线程是 main线程*/
        log.debug("主线程等待结果。。。");
        Integer integer = future.get();
        log.debug("结果：{}",integer);

        /*3.2 可以异步获取结果。
        *  这种方式获取结果的线程 和 执行call方法的线程是一样的*/
        future.addListener(new GenericFutureListener<Future<? super Integer>>() {
            @Override
            public void operationComplete(Future<? super Integer> future) throws Exception {
                log.debug("异步获取结果：{}",future.getNow());
            }
        });
    }
}
