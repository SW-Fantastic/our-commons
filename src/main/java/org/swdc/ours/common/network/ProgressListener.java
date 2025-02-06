package org.swdc.ours.common.network;

public interface ProgressListener {

    void onProgress(NetworkDirection direction, long progress, long length);

}
