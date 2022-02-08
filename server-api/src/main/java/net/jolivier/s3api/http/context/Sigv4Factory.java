package net.jolivier.s3api.http.context;

import org.glassfish.hk2.api.Factory;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import net.jolivier.s3api.auth.AwsSigV4;

/**
 * Context factory for an AWSV4 signature on a particular request.
 * 
 * @author josho
 *
 */
public class Sigv4Factory implements Factory<AwsSigV4> {

	private ContainerRequestContext context;

	@Inject
	public void setContext(ContainerRequestContext context) {
		this.context = context;
	}

	@Override
	public AwsSigV4 provide() {
		return (AwsSigV4) context.getProperty("sigv4");
	}

	@Override
	public void dispose(AwsSigV4 instance) {
	}

}
