package net.jolivier.s3api.launch;

import java.net.URI;

import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jolivier.s3api.http.ApiPoint;
import net.jolivier.s3api.impl.RequestLogger;
import net.jolivier.s3api.impl.S3Server;
import net.jolivier.s3api.memory.S3MemoryImpl;

/**
 * Default entry point to start the s3 server. Hard coded to listen on
 * localhost:9090. You should generally just call
 * {@link net.jolivier.s3api.impl.S3Server#createServer(URI)}
 * 
 * @author josho
 *
 */
public class EntryPoint {

	public static void main(String[] args) throws Exception {

		final Logger logger = LoggerFactory.getLogger(EntryPoint.class);
		SigTerm.configure();

		ApiPoint.configure(S3MemoryImpl.INSTANCE, S3MemoryImpl.INSTANCE);

		Server server = S3Server.createServer(URI.create("http://" + "0.0.0.0" + ":" + 9090));

		RequestLogger.install(server, RequestLogger.DEFAULT_FORMAT);

		SigTerm.register(() -> {
			server.stop();
			server.destroy();
		});

		server.start();

		logger.info("Server started");

		server.join();

	}

}
