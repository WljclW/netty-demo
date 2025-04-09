package _02netty_base.future_promise;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * Future接口的本质————在线程之间传递结果或数据的容器。
 *   并且获取结果的过程是被动的过程，整个过程无法干预结果的产生，由线程来自动执行处理
 * 逻辑 并 填充结果返回。
 *
 * */
@Slf4j
public class TestJDKFuture {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Logger log = LoggerFactory.getLogger(TestJDKFuture.class);
        /*1. 创建线程池*/
        ExecutorService service = Executors.newFixedThreadPool(2);
        /*2. 向线程池提交任务*/
        Future<Integer> future = service.submit(new Callable<Integer>() {
            /*在线程池中执行call方法的逻辑，有返回值。callable泛型指定返回值类型*/
            @Override
            public Integer call() throws Exception {
                log.debug("执行计算");
                Thread.sleep(3000);
                return 50;
            }
        });
        /*3. 主线程通过 future 来获取结果 */
        log.debug("等待结果.........");
        Integer integer = future.get();
        System.out.println(integer);
        log.debug("结果：{}",integer);
        service.shutdown(); //如果没有关闭操作，则程序不会停止。线程池会一直运行。
    }
}
