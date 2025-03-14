package org.swdc.ours.common.network;

import org.swdc.ours.common.helper.ProgressListener;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class NetworkAsync implements Runnable {

    private CountDownLatch latch = new CountDownLatch(1);


    /**
     * 异步请求处理，主要的逻辑处理函数，返回结果类型为T，
     * 通常由Requester创建。
     */
    private BiConsumer<ProgressListener, ByteHandler>  asyncHandler;


    /**
     * 字节流处理，主要用于大量数据的传输，例如文件下载。
     */
    private ByteHandler byteHandler;

    /**
     * 异常处理，主要用于捕获异步请求过程中的异常。
     */
    private Consumer<Exception> exceptionConsumer;

    /**
     * 进度监听器，主要用于监控异步请求的进度。
     */
    private ProgressListener progressListener;

    private Consumer<Void> completeHandler;

    public NetworkAsync(BiConsumer<ProgressListener, ByteHandler> supplier) {
        this.asyncHandler = supplier;
    }


    public NetworkAsync progressHandler(ProgressListener progressListener) {
        this.progressListener = progressListener;
        return this;
    }

    public NetworkAsync byteHandler(ByteHandler byteConsumer) {
        this.byteHandler = byteConsumer;
        return this;
    }

    public NetworkAsync then(Consumer<Void> completeHandler) {
        this.completeHandler = completeHandler;
        return this;
    }

    public ProgressListener getProgressListener() {
        return progressListener;
    }

    @Override
    public void run() {
        try {
            latch.await();
            if (asyncHandler == null) {
                return;
            }
            try {
                asyncHandler.accept(progressListener,byteHandler);
            } catch (Exception e) {
                if (exceptionConsumer != null) {
                    exceptionConsumer.accept(e);
                } else {
                    throw new RuntimeException(e);
                }
            }
            if (completeHandler != null) {
                completeHandler.accept(null);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void send(ThreadPoolExecutor executor) {
        executor.execute(this);
        latch.countDown();
    }

    public void send() {
        CompletableFuture.runAsync(this);
        latch.countDown();
    }


    public NetworkAsync cache(Consumer<Exception> exceptionConsumer) {
        this.exceptionConsumer = exceptionConsumer;
        return this;
    }


}
