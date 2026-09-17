package com.github.novicezk.midjourney.support;


import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.Constants;
import com.github.novicezk.midjourney.ProxyProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
public class ApiAuthorizeInterceptor implements HandlerInterceptor {
	public static final String SLOW_MODE_ATTRIBUTE = "mj.slow-mode";
	private final ProxyProperties properties;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		if (CharSequenceUtil.isBlank(this.properties.getApiSecret())
				&& CharSequenceUtil.isBlank(this.properties.getApiSecretSlow())) {
			return true;
		}
		String apiSecret = request.getHeader(Constants.API_SECRET_HEADER_NAME);
		if (CharSequenceUtil.equals(apiSecret, this.properties.getApiSecret())) {
			return true;
		}
		if (CharSequenceUtil.equals(apiSecret, this.properties.getApiSecretSlow())) {
			if (isSlowModeAllowed(request)) {
				request.setAttribute(SLOW_MODE_ATTRIBUTE, Boolean.TRUE);
				return true;
			}
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return false;
		}
		if (CharSequenceUtil.isNotBlank(apiSecret)) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		}
		return false;
	}

	private boolean isSlowModeAllowed(HttpServletRequest request) {
		String path = request.getRequestURI();
		String contextPath = request.getContextPath();
		if (CharSequenceUtil.isNotBlank(contextPath) && path.startsWith(contextPath)) {
			path = path.substring(contextPath.length());
		}
		if ("POST".equalsIgnoreCase(request.getMethod()) && "/submit/imagine".equals(path)) {
			return true;
		}
		if (path.startsWith("/task/") && "GET".equalsIgnoreCase(request.getMethod())) {
			return true;
		}
		return "/task/list-by-condition".equals(path) && "POST".equalsIgnoreCase(request.getMethod());
	}

}
