package netty_base.future_promise;

import io.netty.channel.DefaultChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;

@Slf4j
public class TestNettyPromise {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        /*1. 准备EventLoop对象。创建Promise时需要*/
        EventLoop eventLoop = new NioEventLoopGroup().next();
        /*2. 主动创建Promise，作为结果容器*/
        DefaultPromise<Integer> promise = new DefaultPromise<>(eventLoop);
        //一个新的线程去执行计算逻辑。thread-0
        new Thread(()->{
            log.debug("执行计算。。。");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            promise.setSuccess(55);
        }).start();

        log.debug("等待结果。。。");
        log.debug("结果：{}",promise.get()); //阻塞等待结果

        /*3. 添加监听器异步获取结果。
        * 此时收到结果的线程(即打印"收到结果：{}")是 nioEventLoopGroup中的一个线程*/
        promise.addListener(new GenericFutureListener<Future<? super Integer>>() {
            @Override
            public void operationComplete(Future<? super Integer> future) throws Exception {
                log.debug("收到结果：{}",future.getNow());
            }
        });
    }
}
