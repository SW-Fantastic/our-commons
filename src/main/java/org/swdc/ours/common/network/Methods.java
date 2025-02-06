package org.swdc.ours.common.network;

public enum Methods {

    /**
     * HTTP Get方法
     * 从服务器读取资源
     */
    GET,

    /**
     * HTTP Post方法
     * 向服务器提交数据，通常用于提交表单数据或者上传文件等操作。
     */
    POST,

    /**
     * HTTP Put方法
     * 通常用于更新。
     */
    PUT,

    /**
     * HTTP Delete方法
     * 通常用于删除。
     */
    DELETE,

    /**
     * HTTP PROPFIND方法
     * WebDAV协议中用于查询资源属性。
     */
    PROPFIND,

    /**
     * HTTP MKCOL方法
     * WebDAV协议中用于创建新的集合（类似于文件夹）。
     */
    MKCOL,

    /**
     * HTTP COPY方法
     * WebDAV协议中用于复制资源。
     */
    COPY,

    /**
     * HTTP MOVE方法
     * WebDAV协议中用于移动或者重命名资源。
     */
    MOVE,

    /**
     * HTTP LOCK方法
     * WebDAV协议中用于锁定资源，以防止同时修改。
     */
    LOCK,

    /**
     * HTTP UNLOCK方法
     * WebDAV协议中用于解锁资源。
     */
    UNLOCK;

}
