package org.alveolo.cocoon.springification.component;

import java.lang.reflect.Method;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TryCatchFinally;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.NoOp;

import org.apache.cocoon.sax.AbstractSAXTransformer;
import org.apache.cocoon.servlet.util.HttpContextHelper;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.web.servlet.tags.form.CheckboxTag;
import org.springframework.web.servlet.tags.form.CheckboxesTag;
import org.springframework.web.servlet.tags.form.ErrorsTag;
import org.springframework.web.servlet.tags.form.FormTag;
import org.springframework.web.servlet.tags.form.HiddenInputTag;
import org.springframework.web.servlet.tags.form.InputTag;
import org.springframework.web.servlet.tags.form.LabelTag;
import org.springframework.web.servlet.tags.form.OptionTag;
import org.springframework.web.servlet.tags.form.OptionsTag;
import org.springframework.web.servlet.tags.form.PasswordInputTag;
import org.springframework.web.servlet.tags.form.RadioButtonTag;
import org.springframework.web.servlet.tags.form.RadioButtonsTag;
import org.springframework.web.servlet.tags.form.SelectTag;
import org.springframework.web.servlet.tags.form.TextareaTag;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


public class SpringFormTransformer extends AbstractSAXTransformer {
	private static final Logger LOG = Logger.getLogger(SpringFormTransformer.class);

	private static final String NAMESPACE = "http://www.springframework.org/tags/form";

	private static final Map<String, Class<? extends Tag>> TAGS = new HashMap<String, Class<? extends Tag>>();
	static {
		TAGS.put("checkboxes", CheckboxesTag.class);
		TAGS.put("checkbox", CheckboxTag.class);
		TAGS.put("errors", ErrorsTag.class);
		TAGS.put("form", FormTag.class);
		TAGS.put("hidden", HiddenInputTag.class);
		TAGS.put("input", InputTag.class);
		TAGS.put("label", LabelTag.class);
		TAGS.put("options", OptionsTag.class);
		TAGS.put("option", OptionTag.class);
		TAGS.put("password", PasswordInputTag.class);
		TAGS.put("radios", RadioButtonsTag.class);
		TAGS.put("radio", RadioButtonTag.class);
		TAGS.put("select", SelectTag.class);
		TAGS.put("textarea", TextareaTag.class);
	}

	private Map<String, Object> parameters;

	private PageContext pageContext;

	private Tag tag;

	private Deque<String> prefixes = new LinkedList<String>();

	@Override
	public void setup(Map<String, Object> parameters) {
		this.parameters = parameters;
	}

	@Override
	public void finish() {
		this.parameters = null;
	}

	@Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		prefixes.push(uri);

		if (!NAMESPACE.equals(uri)) {
			super.startPrefixMapping(prefix, uri);
		}
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		String uri = prefixes.pop();

		if (!NAMESPACE.equals(uri)) {
			super.endPrefixMapping(prefix);
		}
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();

		final ServletContext context = HttpContextHelper.getServletContext(parameters);
		HttpServletRequest request = HttpContextHelper.getRequest(parameters);
		HttpServletResponse response = HttpContextHelper.getResponse(parameters);

		pageContext = JspFactory.getDefaultFactory().getPageContext(
				new FakeServlet(context), request, response, null, false, 0, false);
	}

	@Override
	public void endDocument() throws SAXException {
		super.endDocument();

		pageContext = null;
	}

	@Override
	public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
		if (!NAMESPACE.equals(uri)) {
			super.startElement(uri, localName, name, atts);
			return;
		}

		Class<? extends Tag> cls = TAGS.get(localName);
		if (cls == null) {
			LOG.error("Opening unsupported form tag: " + localName);
			return;
		}

		Tag child = createEchancedTag(cls);
		child.setPageContext(pageContext);

		child.setParent(tag);
		tag = child;

		setTagProperties(atts, child);

		try {
			child.doStartTag();
		} catch (JspException e) {
			handleException(e);
		}
	}

	@Override
	public void endElement(String uri, String localName, String name) throws SAXException {
		if (!NAMESPACE.equals(uri)) {
			super.endElement(uri, localName, name);
			return;
		}

		Class<? extends Tag> cls = TAGS.get(localName);
		if (cls == null) {
			LOG.error("Closing unsupported form tag: " + localName);
			return;
		}

		try {
			tag.doEndTag();
		} catch (JspException e) {
			handleException(e);
		} finally {
			if (tag instanceof TryCatchFinally) {
				TryCatchFinally ttag = (TryCatchFinally) tag;
				ttag.doFinally();
			}

			Tag parent = tag.getParent();
			tag.release();
			tag = parent;
		}
	}

	private void handleException(JspException e) throws SAXException {
		Throwable cause = e.getCause();

		LOG.error("Error while handling forms",
				(cause instanceof SAXException) ? (SAXException) cause : new SAXException(e));
	}

	private <T> T createEchancedTag(Class<T> cls) {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(cls);
		enhancer.setCallbackFilter(new CallbackFilter() {
			@Override
			public int accept(Method method) {
				return method.getName().equals("createTagWriter") ? 0 : 1;
			}
		});
		Callback callback = new MethodInterceptor() {
			@Override
			public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
				return new SaxTagWriter(getSAXConsumer());
			}
		};
		Callback[] callbacks = {callback, NoOp.INSTANCE};
		enhancer.setCallbacks(callbacks);

		@SuppressWarnings("unchecked")
		T tag = (T) enhancer.create();
		return tag;
	}

	private void setTagProperties(Attributes atts, Tag tag) {
		for (int i = 0, len = atts.getLength(); i < len; i++) {
			String name = atts.getQName(i);
			String value = atts.getValue(i);

			try {
				BeanUtils.getPropertyDescriptor(tag.getClass(), name).getWriteMethod().invoke(tag, value);
			} catch (Exception e) {
				throw new BeanInitializationException("Cannot set property: " + name, e);
			}
		}
	}
}
