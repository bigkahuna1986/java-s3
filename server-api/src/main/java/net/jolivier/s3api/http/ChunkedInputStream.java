package net.jolivier.s3api.http;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ChunkedInputStream extends InputStream {

	private static final int CHUNK_SIZE = 262144;
	private static final byte[] CRLF = "\r\n".getBytes(StandardCharsets.UTF_8);
	private static final byte[] DELIMITER = ";".getBytes(StandardCharsets.UTF_8);
	private final InputStream _source;
	private final ByteBuffer _buffer = ByteBuffer.allocate(CHUNK_SIZE);

	private int _remainingInChunk = 0;

	public ChunkedInputStream(InputStream source) {
		_source = source;
	}

	@Override
	public int read() throws IOException {
		if (_remainingInChunk == 0) {
			final byte[] hexLengthBytes = readUntil(DELIMITER);
			if (hexLengthBytes == null)
				return -1;

			_remainingInChunk = Integer.parseInt(new String(hexLengthBytes, StandardCharsets.UTF_8).trim(), 16);

			if (_remainingInChunk == 0)
				return -1;

			readUntil(CRLF);
		}

		_remainingInChunk--;

		return _source.read();
	}

	@Override
	public void close() throws IOException {
		_source.close();
	}

	private byte[] readUntil(byte[] endSequence) throws IOException {
		_buffer.clear();
		while (!endsWith(_buffer.asReadOnlyBuffer(), endSequence)) {
			final int c = _source.read();
			if (c < 0) {
				return null;
			}

			final byte unsigned = (byte) (c & 0xFF);
			_buffer.put(unsigned);
		}

		final byte[] result = new byte[_buffer.position() - endSequence.length];
		_buffer.rewind();
		_buffer.get(result);
		return result;
	}

	private boolean endsWith(ByteBuffer buffer, byte[] endSequence) {
		final int pos = buffer.position();
		if (pos >= endSequence.length) {
			for (int i = 0; i < endSequence.length; i++) {
				if (buffer.get(pos - endSequence.length + i) != endSequence[i])
					return false;
			}

			return true;
		}

		return false;
	}
}