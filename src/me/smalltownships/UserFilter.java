package me.smalltownships;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@WebFilter("/*")
public class UserFilter implements Filter {

	/**
	 * Freshen the current user in the database if there is one.
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		/*
		 * If the current request is not an http request, just ignore.
		 */
		if (!(request instanceof HttpServletRequest)) {
			return;
		}
		
		HttpServletRequest httpRequest = (HttpServletRequest)request;
		
		/* Get the current session, if there is one */
		HttpSession session = httpRequest.getSession(false);
		
		if (session != null) {
			User user = (User)session.getAttribute(User.USER_SESSION);

			if (user != null) {
	            user = user.fresh();

	            // Freshen the current user in the session.
	            session.setAttribute(User.USER_SESSION, user);
			}
		}
		
		/* Forward to the next request */
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
		
	}

}