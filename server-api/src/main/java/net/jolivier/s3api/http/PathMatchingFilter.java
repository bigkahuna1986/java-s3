package net.jolivier.s3api.http;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.PathSegment;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import net.jolivier.s3api.http.context.RequestBucket;

@Provider
@PreMatching
public class PathMatchingFilter implements ContainerRequestFilter {

	private static final Logger _logger = LoggerFactory.getLogger(PathMatchingFilter.class);

	@Context
	private ResourceInfo resourceInfo;

	@Override
	public void filter(ContainerRequestContext ctx) throws IOException {
		final UriInfo uriInfo = ctx.getUriInfo();

		final List<PathSegment> segments = uriInfo.getPathSegments();

		final String bucket = segments.isEmpty() ? "" : segments.get(0).getPath();

		final URI requestUri = uriInfo.getRequestUri();
		final String pathAfter = segments.size() > 1 ? String.join("/",
				segments.subList(1, segments.size()).stream().map(PathSegment::getPath).collect(Collectors.toList()))
				: "/";

		final UriBuilder builder = UriBuilder.fromUri(requestUri).replacePath(pathAfter);

		final URI baseUri = uriInfo.getBaseUri();
		ctx.setProperty("bucket", RequestBucket.of(bucket));
		ctx.setProperty(SignatureFilter.ORIG_URI, requestUri);
		ctx.setRequestUri(baseUri, builder.build());

	}

}
