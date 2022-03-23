package net.jolivier.s3api.impl;

import java.net.URI;

import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import net.jolivier.s3api.http.PathMatchingFilter;
import net.jolivier.s3api.http.ProjectFeature;
import net.jolivier.s3api.http.S3Buckets;
import net.jolivier.s3api.http.S3Objects;
import net.jolivier.s3api.http.SignatureFilter;
import net.jolivier.s3api.http.filter.RangeResponseFilter;
import net.jolivier.s3api.impl.exception.ConflictExceptionMapper;
import net.jolivier.s3api.impl.exception.InternalErrorExceptionMapper;
import net.jolivier.s3api.impl.exception.InvalidAuthExceptionMapper;
import net.jolivier.s3api.impl.exception.NoSuchBucketExceptionMapper;
import net.jolivier.s3api.impl.exception.NoSuchKeyExceptionMapper;
import net.jolivier.s3api.impl.exception.NotImplementedExceptionMapper;
import net.jolivier.s3api.impl.exception.PreconditionFailedExceptionMapper;
import net.jolivier.s3api.impl.exception.RequestFailedExceptionMapper;

public class S3Server {

	public static Server createServer(URI bindUri) {
		final ResourceConfig config = new ResourceConfig();

		config.register(NoSuchKeyExceptionMapper.class);
		config.register(NoSuchBucketExceptionMapper.class);
		config.register(RequestFailedExceptionMapper.class);
		config.register(InvalidAuthExceptionMapper.class);
		config.register(InternalErrorExceptionMapper.class);
		config.register(ConflictExceptionMapper.class);
		config.register(NotImplementedExceptionMapper.class);
		config.register(PreconditionFailedExceptionMapper.class);

		config.register(PathMatchingFilter.class);
		config.register(SignatureFilter.class);
		config.register(RangeResponseFilter.class);

		config.register(ProjectFeature.class);

		// resources
		config.register(S3Buckets.class);
		config.register(S3Objects.class);

		return JettyHttpContainerFactory.createServer(bindUri, config, false);
	}

}
