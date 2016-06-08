package com.thatsit.android;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileSystemException;

public class ProgressInputStream extends InputStream {
	private final long size;
	private long progress, lastUpdate = 0;
	private final InputStream inputStream;
	private final String name;
	private boolean closed = false;
	static FTPProgressCallback ftpProgressCallback;

	public ProgressInputStream(String name, InputStream inputStream, long size, FTPProgressCallback callbaclInstance) {
		this.size = size;
		this.inputStream = inputStream;
		this.name = name;
		this.ftpProgressCallback = callbaclInstance;
	}

	public ProgressInputStream(String name, FileContent content)
			throws FileSystemException {
		this.size = content.getSize();
		this.name = name;
		this.inputStream = content.getInputStream();
	}

	@Override
	public void close() throws IOException {
		super.close();
		if (closed) throw new IOException("already closed");
		closed = true;
	}

	@Override
	public int read() throws IOException {
		int count = inputStream.read();
		if (count > 0)
			progress += count;
		lastUpdate = maybeUpdateDisplay(name, progress, lastUpdate, size);
		return count;
	}
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int count = inputStream.read(b, off, len);
		if (count > 0)
			progress += count;
		lastUpdate = maybeUpdateDisplay(name, progress, lastUpdate, size);
		return count;
	}

	static long maybeUpdateDisplay(String name, long progress, long lastUpdate, long size) {
		ftpProgressCallback.updateProgress(((long)((float)progress/size*100)));
		return lastUpdate;
	}
}