package com.github.novicezk.midjourney.support;

import com.github.novicezk.midjourney.ProxyProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiAuthorizeInterceptorTest {
	@Test
	void slowSecretAllowsImagineAndMarksRequest() throws Exception {
		ProxyProperties properties = new ProxyProperties();
		properties.setApiSecret("fast-secret");
		properties.setApiSecretSlow("slow-secret");
		ApiAuthorizeInterceptor interceptor = new ApiAuthorizeInterceptor(properties);
		MockHttpServletRequest request = request("POST", "/mj/submit/imagine", "slow-secret");

		assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
		assertEquals(Boolean.TRUE, request.getAttribute(ApiAuthorizeInterceptor.SLOW_MODE_ATTRIBUTE));
	}

	@Test
	void slowSecretCannotSubmitChanges() throws Exception {
		ProxyProperties properties = new ProxyProperties();
		properties.setApiSecret("fast-secret");
		properties.setApiSecretSlow("slow-secret");
		ApiAuthorizeInterceptor interceptor = new ApiAuthorizeInterceptor(properties);
		MockHttpServletResponse response = new MockHttpServletResponse();

		assertTrue(!interceptor.preHandle(request("POST", "/mj/submit/change", "slow-secret"), response, new Object()));
		assertEquals(403, response.getStatus());
	}

	@Test
	void fastSecretCanReconnectAccount() throws Exception {
		ProxyProperties properties = new ProxyProperties();
		properties.setApiSecret("fast-secret");
		properties.setApiSecretSlow("slow-secret");
		ApiAuthorizeInterceptor interceptor = new ApiAuthorizeInterceptor(properties);

		assertTrue(interceptor.preHandle(request("POST", "/mj/account/chan-1/reconnect", "fast-secret"), new MockHttpServletResponse(), new Object()));
	}

	@Test
	void slowSecretCannotReconnectAccount() throws Exception {
		ProxyProperties properties = new ProxyProperties();
		properties.setApiSecret("fast-secret");
		properties.setApiSecretSlow("slow-secret");
		ApiAuthorizeInterceptor interceptor = new ApiAuthorizeInterceptor(properties);
		MockHttpServletResponse response = new MockHttpServletResponse();

		assertTrue(!interceptor.preHandle(request("POST", "/mj/account/chan-1/reconnect", "slow-secret"), response, new Object()));
		assertEquals(403, response.getStatus());
	}

	@Test
	void unknownSecretIsUnauthorized() throws Exception {
		ProxyProperties properties = new ProxyProperties();
		properties.setApiSecret("fast-secret");
		properties.setApiSecretSlow("slow-secret");
		ApiAuthorizeInterceptor interceptor = new ApiAuthorizeInterceptor(properties);
		MockHttpServletResponse response = new MockHttpServletResponse();

		assertTrue(!interceptor.preHandle(request("POST", "/mj/submit/imagine", "wrong-secret"), response, new Object()));
		assertEquals(401, response.getStatus());
		assertNull(response.getErrorMessage());
	}

	private MockHttpServletRequest request(String method, String uri, String secret) {
		MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
		request.setContextPath("/mj");
		request.addHeader("mj-api-secret", secret);
		return request;
	}
}
