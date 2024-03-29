package net.jolivier.s3api.http;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.inject.Singleton;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.PathSegment;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;

@Provider
@PreMatching
@Singleton
public class PathMatchingFilter implements ContainerRequestFilter {

	@Override
	public void filter(ContainerRequestContext ctx) throws IOException {
		final UriInfo uriInfo = ctx.getUriInfo();

		final List<PathSegment> segments = uriInfo.getPathSegments();

		final String bucket = segments.isEmpty() ? null : segments.get(0).getPath();

		final URI requestUri = uriInfo.getRequestUri();
		final String key = segments.size() > 1 ? String.join("/",
				segments.subList(1, segments.size()).stream().map(PathSegment::getPath).collect(Collectors.toList()))
				: "/";

		final UriBuilder builder = UriBuilder.fromUri(requestUri).replacePath(key);

		final URI baseUri = uriInfo.getBaseUri();
		ctx.setProperty("bucket", bucket);
		ctx.setProperty("key", key);
		ctx.setProperty(SignatureFilter.ORIG_URI, requestUri);
		ctx.setRequestUri(baseUri, builder.build());

	}

}
