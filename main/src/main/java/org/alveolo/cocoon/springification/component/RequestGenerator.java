package org.alveolo.cocoon.springification.component;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.cocoon.pipeline.ProcessingException;
import org.apache.cocoon.sax.AbstractSAXGenerator;
import org.apache.cocoon.sax.SAXConsumer;
import org.apache.cocoon.servlet.util.HttpContextHelper;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;


public class RequestGenerator extends AbstractSAXGenerator {
	private static final String NAMESPACE = "";
	private static final String ELEM_REQUEST = "request";
	private static final String ELEM_PARAMETERS = "parameters";
	private static final String ELEM_PARAMETER = "parameter";
	private static final String ELEM_ATTRIBUTES = "attributes";
	private static final String ELEM_ATTRIBUTE = "attribute";
	private static final String ATTR_NAME = "name";

	private Map<String, Object> parameters;

	@Override
	public void setup(Map<String, Object> parameters) {
		this.parameters = parameters;
	}

	@Override
	public void finish() {
		this.parameters = null;
	}

	@Override
	public void execute() {
		HttpServletRequest request = HttpContextHelper.getRequest(parameters);

		SAXConsumer consumer = getSAXConsumer();
		try {
			consumer.startDocument();
			startElement(ELEM_REQUEST);
			writeParameters(request);
			writeAttributes(request);
			endElement(ELEM_REQUEST);
			consumer.endDocument();
		} catch (Exception e) {
			throw new ProcessingException(e);
		}
	}

	private void writeParameters(HttpServletRequest request) throws SAXException {
		@SuppressWarnings("unchecked")
		Enumeration<String> names = request.getParameterNames();

		startElement(ELEM_PARAMETERS);
		while (names.hasMoreElements()) {
			String name = names.nextElement();

			for (String value : request.getParameterValues(name)) {
				startElement(ELEM_PARAMETER, ATTR_NAME, name);
				characters(value);
				endElement(ELEM_PARAMETER);
			}
		}
		endElement(ELEM_PARAMETERS);
	}

	private void writeAttributes(HttpServletRequest request) throws JAXBException, SAXException {
		@SuppressWarnings("unchecked")
		Enumeration<String> names = request.getAttributeNames();

		startElement(ELEM_ATTRIBUTES);
		while (names.hasMoreElements()) {
			writeAttribute(request, names.nextElement());
		}
		endElement(ELEM_ATTRIBUTES);
	}

	private void writeAttribute(HttpServletRequest request, String name) throws JAXBException, SAXException {
		writeAttribute(name, request.getAttribute(name));
	}

	private void writeAttribute(String name, Object value) throws JAXBException, SAXException {
		startElement(ELEM_ATTRIBUTE, ATTR_NAME, name);
		if (value.getClass().isAnnotationPresent(XmlRootElement.class)) {
			marshall(value);
		} else if (value instanceof Collection<?>) {
			for (Object o : (Collection<?>) value) {
				marshall(o);
			}
		} else {
			characters(value.toString());
		}
		endElement(ELEM_ATTRIBUTE);
	}

	private void marshall(Object value) throws JAXBException, PropertyException {
		// TODO: use Spring OXM here for flexible Marshaller configuration
		JAXBContext c = JAXBContext.newInstance(value.getClass());
		Marshaller m = c.createMarshaller();
		m.setProperty(Marshaller.JAXB_FRAGMENT, true);
		m.marshal(value, getSAXConsumer());
	}

	private void startElement(String name) throws SAXException {
		getSAXConsumer().startElement(NAMESPACE, name, name, new AttributesImpl());
	}

	private void startElement(String name, String attrName, String attrValue) throws SAXException {
		AttributesImpl atts = new AttributesImpl();
		atts.addAttribute("", attrName, attrName, "CDATA", attrValue);
		getSAXConsumer().startElement(NAMESPACE, name, name, atts);
	}

	private void endElement(String name) throws SAXException {
		getSAXConsumer().endElement(NAMESPACE, name, name);
	}

	private void characters(String value) throws SAXException {
		getSAXConsumer().characters(value.toCharArray(), 0, value.length());
	}
}
