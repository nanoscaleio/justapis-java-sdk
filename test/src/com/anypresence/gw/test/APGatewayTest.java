package com.anypresence.gw.test;

import java.net.HttpURLConnection;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.integration.ClientAndProxy;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.socket.PortFactory;

import static org.mockserver.model.HttpRequest.*;
import static org.mockserver.model.HttpResponse.*;
import static org.mockserver.integration.ClientAndProxy.startClientAndProxy;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.verify.VerificationTimes.exactly;

import com.anypresence.gw.*;

import static org.mockito.Mockito.*;

public final class APGatewayTest {
	@Mock
	HttpURLConnection mockHttpConnection;
	private static ClientAndProxy proxy;
	private ClientAndServer mockServer;
	
    @BeforeClass
    public static void startProxy() {
        proxy = startClientAndProxy(PortFactory.findFreePort());
    }
    
    @Before
    public void startMockServer() {
        mockServer = startClientAndServer(1080);
    }  
    
    @AfterClass
    public static void stopProxy() {
        proxy.stop();
    }
    
    @After
    public void stopMockServer() {
        mockServer.stop();
    }
    
	@Test
	public void test_BuilderSettings() {
		APGateway.Builder builder = new APGateway.Builder();
		builder.url("http://localhost");

		APGateway gw = builder.build();
		Assert.assertEquals("http://localhost", gw.getUrl());
	}

	@Test
	public void test_Connect() {
		APGateway.Builder builder = new APGateway.Builder();
		builder.url("http://localhost:1080/api/v1/foo");
		builder.method(HTTPMethod.GET);

		APGateway gw = builder.build();

		mockServer.when(
				request().withMethod("GET").withPath("/api/v1/foo")
				)
				.respond(
						response()
								.withStatusCode(302)
								.withCookie("sessionId",
										"some_session_id")
								.withHeader("Location",
										"https://www.mock-server.com")
								.withBody("testing123")); 
		
		gw.execute();
		
		String responseBody = gw.readResponse();
		Assert.assertEquals("testing123", responseBody);
	}
	

}
