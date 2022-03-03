package net.jolivier.s3api.http.context;

import org.glassfish.hk2.api.Factory;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;

public class S3ContextFactory implements Factory<S3Context> {

	private ContainerRequestContext context;

	@Inject
	public void setContext(ContainerRequestContext context) {
		this.context = context;
	}

	@Override
	public S3Context provide() {
		return (S3Context) context.getProperty("s3ctx");
	}

	@Override
	public void dispose(S3Context instance) {
	}

}