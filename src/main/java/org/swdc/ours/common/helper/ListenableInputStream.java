package org.swdc.ours.common.helper;

import org.swdc.ours.common.network.CancelledException;

import java.io.IOException;
import java.io.InputStream;

public class ListenableInputStream extends InputStream {

    private final InputStream inputStream;

    private final ProgressListener progressListener;

    private final long length;


    public ListenableInputStream(final InputStream inputStream, final ProgressListener progressListener) throws IOException {
        this.inputStream = inputStream;
        this.progressListener = progressListener;
        this.length = inputStream.available();
    }

    @Override
    public int read() throws IOException {
        try {
            progressListener.onProgress(ProgressDirection.READ, 1, length);
        } catch (CancelledException e) {
            throw new IOException(e);
        }
        return inputStream.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        int read = inputStream.read(b);
        try {
            progressListener.onProgress(ProgressDirection.READ, read, length);
        } catch (CancelledException e) {
            throw new IOException(e);
        }
        return read;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int read = inputStream.read(b, off, len);
        try {
            progressListener.onProgress(ProgressDirection.READ, read, length);
        } catch (CancelledException e) {
            throw new IOException(e);
        }
        return read;
    }

    @Override
    public byte[] readNBytes(int len) throws IOException {
        byte[] bytes = inputStream.readNBytes(len);
        try {
            progressListener.onProgress(ProgressDirection.READ, bytes.length, length);
            return bytes;
        } catch (CancelledException e) {
            throw new IOException(e);
        }
    }

    @Override
    public int readNBytes(byte[] b, int off, int len) throws IOException {
        int read = inputStream.readNBytes(b, off, len);
        try {
            progressListener.onProgress(ProgressDirection.READ, read, length);
            return read;
        } catch (CancelledException e) {
            throw new IOException(e);
        }
    }

    @Override
    public int available() throws IOException {
        return inputStream.available();
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

    @Override
    public long skip(long n) throws IOException {
        return inputStream.skip(n);
    }

    @Override
    public boolean markSupported() {
        return inputStream.markSupported();
    }

    @Override
    public void mark(int readlimit) {
        inputStream.mark(readlimit);
    }

}
