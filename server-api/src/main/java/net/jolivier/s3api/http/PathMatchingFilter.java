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
import net.jolivier.s3api.model.PublicAccessBlockConfiguration;

@Provider
@PreMatching
public class PathMatchingFilter implements ContainerRequestFilter {

	private static final Logger _logger = LoggerFactory.getLogger(PathMatchingFilter.class);

	@Context
	private ResourceInfo resourceInfo;

	@Override
	public void filter(ContainerRequestContext ctx) throws IOException {
		final UriInfo uriInfo = ctx.getUriInfo();

		_logger.info("req {}", uriInfo.getRequestUri());

		final List<PathSegment> segments = uriInfo.getPathSegments();
		final String bucket = segments.get(0).getPath();

		final UriBuilder builder = UriBuilder.fromUri(uriInfo.getRequestUri())
				.replacePath(
						segments.size() > 1
								? String.join("/", segments.subList(1, segments.size() - 1).stream()
										.map(PathSegment::getPath).collect(Collectors.toList()))
								: "/");

		final URI baseUri = uriInfo.getBaseUri();
		URI nextUri = builder.build();
		_logger.info("setRequestUri(\"{}\",\"{}\"):{}", nextUri, baseUri, bucket);
		ctx.setRequestUri(baseUri, nextUri);
		ctx.setProperty("bucket", RequestBucket.of(bucket));
		PublicAccessBlockConfiguration accessPolicy = ApiPoint.data().internalPublicAccessBlock(bucket);
	}

}
