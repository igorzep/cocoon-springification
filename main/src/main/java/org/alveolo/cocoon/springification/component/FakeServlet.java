package org.alveolo.cocoon.springification.component;


import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;


/**
 * Fake servlet as Cocoon XMLSitemapServlet does not save information about itself in parameters :(
 * TODO: Contribute this functionality to main Cocoon, or provide own servlet
 * Or use Spring DispatcherServlet with special ViewResolver
 */
public final class FakeServlet implements Servlet {
	final ServletConfig config;

	public FakeServlet(ServletContext context) {
		config = new FakeServletConfig(context);
	}

	@Override
	public void service(ServletRequest req, ServletResponse res) {}

	@Override
	public void init(ServletConfig config) throws ServletException {}

	@Override
	public void destroy() {}

	@Override
	public String getServletInfo() {
		return "Fake servlet";
	}

	@Override
	public ServletConfig getServletConfig() {
		return config;
	}
}
