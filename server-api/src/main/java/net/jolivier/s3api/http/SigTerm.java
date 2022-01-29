package net.jolivier.s3api.http;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.misc.Signal;

public enum SigTerm {
	;
	private static final Logger _logger = LoggerFactory.getLogger(SigTerm.class);

	public static interface TermHandler {
		public void term() throws Exception;
	}

	private static final List<TermHandler> _runnables = new LinkedList<>();

	private static final AtomicBoolean _shutdown = new AtomicBoolean(false);

	public static void configure() {
		Signal.handle(new Signal("TERM"), signal -> {
			shutdown("sigterm");
			System.exit(signal.getNumber() + 0200);
		});

		Runtime.getRuntime().addShutdownHook(new Thread("Shutdown") {
			@Override
			public void run() {
				shutdown("shutdown-hooks");
			}
		});
	}

	public static void register(TermHandler r) {
		_runnables.add(Objects.requireNonNull(r, "r"));
	}

	private static final void shutdown(String reason) {
		_logger.info(reason);
		if (_shutdown.compareAndSet(false, true)) {
			_runnables.forEach(r -> {
				try {
					r.term();
				} catch (Throwable e) {
					_logger.error("", e);
				}
			});
		}
	}

}
