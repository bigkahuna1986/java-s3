package net.jolivier.s3api.http;

import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.ext.Provider;
import net.jolivier.s3api.auth.AwsSigV4;
import net.jolivier.s3api.auth.S3Context;
import net.jolivier.s3api.http.context.S3ContextFactory;
import net.jolivier.s3api.http.context.Sigv4Factory;

/**
 * Registers Jersey context factories for {@link net.jolivier.s3api.model.User}
 * 
 * @author josho
 *
 */
@Provider
public class ProjectFeature implements Feature {

	@Override
	public boolean configure(FeatureContext context) {

		context.register(new AbstractBinder() {
			@Override
			protected void configure() {
				bindFactory(S3ContextFactory.class).to(S3Context.class).proxy(false).proxyForSameScope(true)
						.in(PerLookup.class);

			}
		});

		context.register(new AbstractBinder() {
			@Override
			protected void configure() {
				bindFactory(Sigv4Factory.class).to(AwsSigV4.class).proxy(false).proxyForSameScope(true)
						.in(PerLookup.class);

			}
		});

		return true;

	}
}
