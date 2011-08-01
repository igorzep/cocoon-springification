package org.alveolo.cocoon.springification.component;

import java.util.Deque;
import java.util.LinkedList;

import javax.servlet.jsp.JspException;

import org.apache.cocoon.sax.SAXConsumer;
import org.apache.commons.io.output.NullWriter;
import org.springframework.web.servlet.tags.form.TagWriter;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;


public class SaxTagWriter extends TagWriter {
	private static final String HTML_NAMESPACE = "";

	private Deque<String> prefixes = new LinkedList<String>();

	private SAXConsumer consumer;

	private AttributesImpl atts;

	public SaxTagWriter(SAXConsumer consumer) {
		super(new NullWriter());

		this.consumer = consumer;
	}

	@Override
	public void startTag(String tagName) throws JspException {
		super.startTag(tagName);

		closeTagAndMarkAsBlock();

		prefixes.push(tagName);
		atts = new AttributesImpl();
	}

	@Override
	public void writeAttribute(String attributeName, String attributeValue) throws JspException {
		super.writeAttribute(attributeName, attributeValue);

		atts.addAttribute("", attributeName, attributeName, "CDATA", attributeValue);
	}

	@Override
	public void appendValue(String value) throws JspException {
		super.appendValue(value);

		closeTagAndMarkAsBlock();

		characters(value);
	}

	@Override
	public void forceBlock() throws JspException {
		super.forceBlock();

		closeTagAndMarkAsBlock();
	}

	@Override
	public void endTag(boolean enforceClosingTag) throws JspException {
		super.endTag(enforceClosingTag);

		closeTagAndMarkAsBlock();

		endElement(prefixes.pop());
	}

	private void closeTagAndMarkAsBlock() throws JspException {
		if (atts != null) {
			startElement(prefixes.peek());
			atts = null;
		}
	}

	// Wrap SAX exceptions to JSP exceptions

	private void startElement(String localName) throws JspException {
		try {
			consumer.startElement(HTML_NAMESPACE, localName, localName, atts);
		} catch (SAXException e) {
			throw new JspException(e);
		}
	}

	private void endElement(String localName) throws JspException {
		try {
			consumer.endElement(HTML_NAMESPACE, localName, localName);
		} catch (SAXException e) {
			throw new JspException(e);
		}
	}

	private void characters(String value) throws JspException {
		try {
			char[] ch = value.toCharArray();
			consumer.characters(ch, 0, ch.length);
		} catch (SAXException e) {
			throw new JspException(e);
		}
	}
}
