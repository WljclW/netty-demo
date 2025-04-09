package _04chat.protocol;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 序列号生成器
 * */
public abstract class SequenceIdGenerator {
    private AtomicInteger sequenceId = new AtomicInteger();

    static int nextId(){
        return SequenceIdGenerator.nextId();
    }
}
