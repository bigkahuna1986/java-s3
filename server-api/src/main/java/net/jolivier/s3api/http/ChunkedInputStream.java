package net.jolivier.s3api.http;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferOverflowException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.primitives.Ints;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import net.jolivier.s3api.RequestFailedException;

public class ChunkedInputStream extends InputStream {

	private static final ByteBufAllocator POOL = PooledByteBufAllocator.DEFAULT;

	private static final byte[] CRLF = "\r\n".getBytes(UTF_8);
	private static final byte[] DELIMITER = ";".getBytes(UTF_8);

	private final InputStream _source;
	private final ByteBuf _byteBuf = POOL.directBuffer(8 * 1024);
	private final AtomicBoolean _released = new AtomicBoolean();

	private int _remainingInChunk = 0;

	public ChunkedInputStream(InputStream source) {
		_source = source;
	}

	private final void release() {
		if (_released.compareAndSet(false, true))
			_byteBuf.release();
	}

	@Override
	public void close() throws IOException {
		release();
		_source.close();
	}

	private boolean endsWith(ByteBuf buffer, byte[] endSequence) {
		final int pos = buffer.readerIndex();
		if (pos >= endSequence.length) {
			for (int i = 0; i < endSequence.length; i++)
				if (buffer.getByte(pos - endSequence.length + i) != endSequence[i])
					return false;

			return true;
		}

		return false;
	}

	// Really need to do a bulk read.
	private byte[] readUntil(byte[] endSequence) throws IOException {
		try {
			_byteBuf.clear();
			while (!endsWith(_byteBuf.asReadOnly(), endSequence)) {
				final int c = _source.read();
				if (c < 0)
					return null;

				final byte unsigned = (byte) (c & 0xFF);
				_byteBuf.writeByte(unsigned);
			}

			final byte[] result = new byte[_byteBuf.writerIndex() - endSequence.length];
			_byteBuf.resetReaderIndex();
			_byteBuf.readBytes(result);
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
			if (hexLengthBytes == null) {
				release();
				return -1;
			}

			_remainingInChunk = parseOrThrow(hexLengthBytes);

			if (_remainingInChunk == 0) {
				release();
				return -1;
			}

			readUntil(CRLF);
		}

		_remainingInChunk--;

		return _source.read();
	}
}