package net.jolivier.s3api.http.context;

import org.glassfish.hk2.api.Factory;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;

public class RequestBucketFactory implements Factory<RequestBucket> {

	private ContainerRequestContext context;

	@Inject
	public void setContext(ContainerRequestContext context) {
		this.context = context;
	}

	@Override
	public RequestBucket provide() {
		return (RequestBucket) context.getProperty("bucket");
	}

	@Override
	public void dispose(RequestBucket instance) {
	}

}