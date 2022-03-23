package net.jolivier.s3api.http;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import com.google.common.primitives.Ints;

import net.jolivier.s3api.RequestFailedException;

public class ChunkedInputStream extends InputStream {

	static {
	}

	private static final byte[] CRLF = "\r\n".getBytes(UTF_8);
	private static final byte[] DELIMITER = ";".getBytes(UTF_8);

	private final InputStream _source;
	private final ByteBuffer _byteBuf = ByteBuffer.allocate(256 * 1024);

	private int _remainingInChunk = 0;

	public ChunkedInputStream(InputStream source) {
		_source = source;
	}

	@Override
	public void close() throws IOException {
		_source.close();
	}

	private boolean endsWith(ByteBuffer buffer, byte[] endSequence) {
		final int pos = buffer.position();
		if (pos >= endSequence.length) {
			for (int i = 0; i < endSequence.length; i++)
				if (buffer.get(pos - endSequence.length + i) != endSequence[i])
					return false;

			return true;
		}

		return false;
	}

	// Really need to do a bulk read.
	private byte[] readUntil(byte[] endSequence) throws IOException {
		try {
			_byteBuf.clear();
			while (!endsWith(_byteBuf.asReadOnlyBuffer(), endSequence)) {
				final int c = _source.read();
				if (c < 0)
					return null;

				final byte unsigned = (byte) (c & 0xFF);
				_byteBuf.put(unsigned);
			}

			final byte[] result = new byte[_byteBuf.position() - endSequence.length];
			_byteBuf.rewind();
			_byteBuf.get(result);
			return result;
		} catch (BufferOverflowException e) {
			throw new RequestFailedException("Invalid chunk length");
		}
	}

	private static final int parseOrThrow(byte[] in) {
		String trimmed = new String(in, StandardCharsets.UTF_8).trim();
		Integer parsed = Ints.tryParse(trimmed, 16);
		if (parsed == null)
			throw new IllegalArgumentException("Invalid integer: " + trimmed);

		return parsed;
	}

	@Override
	public int read() throws IOException {
		if (_remainingInChunk == 0) {
			final byte[] hexLengthBytes = readUntil(DELIMITER);
			if (hexLengthBytes == null)
				return -1;

			_remainingInChunk = parseOrThrow(hexLengthBytes);

			if (_remainingInChunk == 0)
				return -1;

			readUntil(CRLF);
		}

		_remainingInChunk--;

		return _source.read();
	}
}