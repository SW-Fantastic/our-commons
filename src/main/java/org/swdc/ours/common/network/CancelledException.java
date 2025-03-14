package org.swdc.ours.common.network;

/**
 * 此异常用于标记网络请求被取消
 * 当此异常被抛出时，通常意味着用户取消了正在进行的网络请求。
 */
public class CancelledException extends RuntimeException {
    public CancelledException(String message) {
        super(message);
    }
}
