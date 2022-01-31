package net.jolivier.s3api.http;

import org.glassfish.hk2.api.Factory;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import net.jolivier.s3api.model.User;

public class S3UserFactory implements Factory<User> {

	private ContainerRequestContext context;

	@Inject
	public void setContext(ContainerRequestContext context) {
		this.context = context;
	}

	@Override
	public User provide() {
		return (User) context.getProperty("s3user");
	}

	@Override
	public void dispose(User instance) {
	}

}
