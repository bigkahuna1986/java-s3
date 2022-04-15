package net.jolivier.s3api.http.context;

import org.glassfish.hk2.api.Factory;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import net.jolivier.s3api.auth.S3Context;
import net.jolivier.s3api.http.SignatureFilter;

public class S3ContextFactory implements Factory<S3Context> {

	private ContainerRequestContext context;

	@Inject
	public void setContext(ContainerRequestContext context) {
		this.context = context;
	}

	@Override
	public S3Context provide() {
		return (S3Context) context.getProperty(SignatureFilter.CTX_KEY);
	}

	@Override
	public void dispose(S3Context instance) {
	}

}