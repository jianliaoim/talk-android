package com.teambition.talk.entity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import retrofit.mime.TypedFile;

/**
 * Created by jgzhu on 10/10/14.
 */
public class CountingTypedFile extends TypedFile {

    public interface ProgressListener {
        void transferred(long bytes);
    }

    private static final int BUFFER_SIZE = 4096;

    private final ProgressListener listener;

    public CountingTypedFile(String mimeType, File file, ProgressListener listener) {
        super(mimeType, file);
        this.listener = listener;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        FileInputStream in = new FileInputStream(super.file());
        long total = 0;
        try {
            int read;
            while ((read = in.read(buffer)) != -1) {
                total += read;
                this.listener.transferred(total);
                out.write(buffer, 0, read);
            }
        } finally {
            in.close();
        }
    }
}
