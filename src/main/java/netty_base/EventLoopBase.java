package netty_base;

import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.NettyRuntime;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mini-zch
 * @date 2025/4/3 19:06
 */
/**
 * [说明几个用到的netty提供的类]
 * 1. MultithreadEventLoopGroup：EventLoopGroup的抽象基类，用来实现在同一时间处理用多线程来处理任务
 * 2.
 * */
@Slf4j
public class EventLoopBase {
    public static void main(String[] args) {
        Logger log = LoggerFactory.getLogger(EventLoopBase.class);
        /*EventLoopGroup——事件循环组：处理IO事件、普通任务、定时任务*/
        EventLoopGroup eventExecutors = new NioEventLoopGroup(4); //创建的事件循环组有4个线程
        /*DefaultEventLoopGroup：处理普通任务、定时任务*/
        EventLoopGroup eventExecutors1 = new DefaultEventLoopGroup(4);
        System.out.println(NettyRuntime.availableProcessors());
        System.out.println(eventExecutors.next()); //通过next会得到组里面的下一个EventLoop
        System.out.println(eventExecutors.next());
        System.out.println(eventExecutors.next());
        System.out.println(eventExecutors.next());

        log.debug("{}", eventExecutors.next());
        log.debug("{}", eventExecutors.next());
        log.debug("{}", eventExecutors.next());
        log.debug("{}", eventExecutors.next());
        log.debug("============================");
        log.debug("{}", eventExecutors1.next());
        log.debug("{}", eventExecutors1.next());
        log.debug("{}", eventExecutors1.next());
        log.debug("{}", eventExecutors1.next());

        /*
        * ###执行普通任务.
        * 【说明】连续调用next获取的是线程组中的另外一个EventLoop，因此下面的连续两次调用会发现日志打印
        *   出来的不是一个线程(因为线程池中不止一个线程)
        * */
        eventExecutors.next().execute(()->{
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            log.debug("时间循环组中的日志打印");
        });
        eventExecutors.next().execute(()->{
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            log.debug("时间循环组中的日志打印");
        });

        /*
        * #####执行定时任务。但是整个定时任务是在同一个线程上执行的。
        * scheduleAtFixedRate：以固定的频率运行任务。连续两次执行任务的时间间隔1s，会尽可能的保证1s。【说明】如果前面任务的执行时间
        *   超出1s，则前面任务执行结束后，后面的依次执行就立马执行，不会等待了
        * */
        eventExecutors.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                log.debug("执行定时任务。。。。。。。。。。。");
            }
        }, 5000,1000,java.util.concurrent.TimeUnit.MILLISECONDS);

        log.debug("主线程的日志打印");
    }
}
