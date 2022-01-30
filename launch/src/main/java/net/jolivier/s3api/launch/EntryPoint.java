package net.jolivier.s3api.launch;

import java.net.URI;

import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jolivier.s3api.http.S3Buckets;
import net.jolivier.s3api.http.S3Objects;
import net.jolivier.s3api.http.SigTerm;
import net.jolivier.s3api.http.SignatureFilter;
import net.jolivier.s3api.impl.exception.ObjectNotFoundExceptionMapper;
import net.jolivier.s3api.impl.exception.RequestFailedExceptionMapper;

public class EntryPoint {

	public static void main(String[] args) throws Exception {

		final Logger logger = LoggerFactory.getLogger(EntryPoint.class);
		SigTerm.configure();

		final ResourceConfig config = new ResourceConfig();

		config.property(ServerProperties.WADL_FEATURE_DISABLE, Boolean.TRUE);

		config.register(ObjectNotFoundExceptionMapper.class);
		config.register(RequestFailedExceptionMapper.class);
		config.register(SignatureFilter.class);

		// resources
		config.register(S3Buckets.class);
		config.register(S3Objects.class);

		final URI uri = URI.create("http://" + "0.0.0.0" + ":" + 9090);
		final Server server = JettyHttpContainerFactory.createServer(uri, false);

		SigTerm.register(() -> {
			server.stop();
			server.destroy();
		});

		server.start();

		logger.info("Server started");

		server.join();

	}

}