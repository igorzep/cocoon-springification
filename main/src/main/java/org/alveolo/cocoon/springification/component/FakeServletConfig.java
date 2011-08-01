package org.alveolo.cocoon.springification.component;

import java.util.Enumeration;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;


@SuppressWarnings("rawtypes")
public final class FakeServletConfig implements ServletConfig {
	public Vector empty = new Vector();

	final ServletContext context;

	public FakeServletConfig(ServletContext context) {
		this.context = context;
	}

	@Override
	public String getServletName() {
		return "FakeServlet";
	}

	@Override
	public ServletContext getServletContext() {
		return context;
	}

	@Override
	public Enumeration getInitParameterNames() {
		return empty.elements();
	}

	@Override
	public String getInitParameter(String name) {
		return null;
	}
}
