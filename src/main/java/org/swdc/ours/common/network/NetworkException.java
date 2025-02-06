package org.swdc.ours.common.network;

public class NetworkException extends RuntimeException {

    private int code;

    public NetworkException(int code) {
        super("Network error: " + code);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
