package net.jolivier.s3api.model;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import uk.co.lucasweb.aws.v4.signer.HttpRequest;
import uk.co.lucasweb.aws.v4.signer.Signer;
import uk.co.lucasweb.aws.v4.signer.credentials.AwsCredentials;

public class AwsSignatureTests {

	@Test
	public void testV4Sig() throws URISyntaxException {
		String contentSha256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
		HttpRequest request = new HttpRequest("GET",
				new URI("https://examplebucket.s3.amazonaws.com?max-keys=2&prefix=J"));
		String signature = Signer.builder().awsCredentials(new AwsCredentials("ACCESSLEY", "SECRETKEY"))
				.header("Host", "examplebucket.s3.amazonaws.com").header("x-amz-date", "20130524T000000Z")
				.header("x-amz-content-sha256", contentSha256)
				
				.buildS3(request, contentSha256).getSignature();
		
		System.out.println("sig " + signature);
	}

}
