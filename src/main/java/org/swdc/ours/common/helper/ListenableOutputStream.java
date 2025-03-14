package org.swdc.ours.common.helper;

import org.swdc.ours.common.network.CancelledException;

import java.io.IOException;
import java.io.OutputStream;

public class ListenableOutputStream extends OutputStream {

    private final OutputStream output;

    private final ProgressListener listener;

    private final long length;


    public ListenableOutputStream(OutputStream output, ProgressListener listener, long length) {
        this.output = output;
        this.listener = listener;
        this.length = length;
    }

    @Override
    public void write(int b) throws IOException {
        output.write(b);
        try {
            listener.onProgress(ProgressDirection.WRITE, 1, length);
        } catch (CancelledException e){
            throw new IOException(e);
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        output.write(b);
        try {
            listener.onProgress(ProgressDirection.WRITE, b.length, length);
        } catch (CancelledException e){
            throw new IOException(e);
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        output.write(b, off, len);
        try {
            listener.onProgress(ProgressDirection.WRITE, len, length);
        } catch (CancelledException e){
            throw new IOException(e);
        }
    }

    @Override
    public void flush() throws IOException {
        output.flush();
    }

    @Override
    public void close() throws IOException {
        output.close();
    }



}
