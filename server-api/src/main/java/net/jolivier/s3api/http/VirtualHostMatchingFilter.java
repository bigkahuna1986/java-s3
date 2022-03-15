package net.jolivier.s3api.http;

import java.io.IOException;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;
import net.jolivier.s3api.NoSuchBucketException;

@Provider
@PreMatching
public class VirtualHostMatchingFilter implements ContainerRequestFilter {

	@Context
	private ResourceInfo resourceInfo;

	@Override
	public void filter(ContainerRequestContext ctx) throws IOException {
		String host = ctx.getHeaderString("Host");
		String base = ApiPoint.domainBase();
		int index = host.lastIndexOf(base);
		if (index < 0)
			throw new NoSuchBucketException(host);

		final String bucket = host.substring(0, index);

		ctx.setProperty("bucket", bucket);
		ctx.setProperty("key", ctx.getUriInfo().getRequestUri().getPath().replaceFirst("/", ""));

	}

}