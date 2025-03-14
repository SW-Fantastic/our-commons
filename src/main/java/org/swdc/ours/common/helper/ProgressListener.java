package org.swdc.ours.common.helper;

public interface ProgressListener {

    void onProgress(ProgressDirection direction, long bytes, long length);

}
