package de.raptor2101.GalDroid.WebGallery;

import java.io.IOException;
import java.io.InputStream;

public class Stream extends InputStream {
    private final InputStream mStream;
    private final long mLength;

    public Stream(InputStream inputSream, long length) {
	mStream = inputSream;
	mLength = length;
    }

    @Override
    public int available() throws IOException {
	return mStream.available();
    }

    @Override
    public void close() throws IOException {
	mStream.close();
    }

    @Override
    public void mark(int readlimit) {
	mStream.mark(readlimit);
    }

    @Override
    public boolean markSupported() {
	return mStream.markSupported();
    }

    @Override
    public int read() throws IOException {
	return mStream.read();
    }

    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
	return mStream.read(buffer, offset, length);
    }

    @Override
    public int read(byte[] buffer) throws IOException {
	return mStream.read(buffer);
    }

    @Override
    public synchronized void reset() throws IOException {
	mStream.reset();
    }

    @Override
    public long skip(long byteCount) throws IOException {
	return mStream.skip(byteCount);
    }

    public long getContentLength() {
	return mLength;
    }
}
