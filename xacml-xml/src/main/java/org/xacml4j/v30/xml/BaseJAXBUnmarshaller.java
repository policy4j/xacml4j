package org.xacml4j.v30.xml;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshallerHandler;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.EventReaderDelegate;
import javax.xml.stream.util.StreamReaderDelegate;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.marshal.Unmarshaller;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import com.google.common.base.Preconditions;

abstract class BaseJAXBUnmarshaller <T>
	implements Unmarshaller<T>
{
	/** Log */
	private final Logger log = LoggerFactory.getLogger(getClass());

	private final JAXBContext context;

	protected BaseJAXBUnmarshaller(JAXBContext context){
		Preconditions.checkArgument(context != null);
		this.context = context;
	}

	@Override
	public final T unmarshal(Object source) throws SyntaxException, IOException
	{
		Preconditions.checkNotNull(source);
		try{
			javax.xml.bind.Unmarshaller u = context.createUnmarshaller();
			u.setEventHandler(new ValidationEventHandler() {
				@Override
				public boolean handleEvent(ValidationEvent event) {
					log.debug("{}", event);
					return true;
				}
			});

			if (source instanceof JAXBElement<?>) {
				return create((JAXBElement<?>) source);
			}
			// DOM and StAX were parsed by the caller. Validate booleans without creating another parser.
			if (source instanceof DOMSource) {
				source = ((DOMSource) source).getNode();
			}
			if (source instanceof Node) {
				validateDom((Node) source);
				return create((JAXBElement<?>) u.unmarshal((Node) source));
			}
			if (source instanceof StAXSource) {
				StAXSource stax = (StAXSource) source;
				source = stax.getXMLStreamReader() != null ? stax.getXMLStreamReader() : stax.getXMLEventReader();
			}
			if (source instanceof XMLStreamReader) {
				return create((JAXBElement<?>) u.unmarshal(new BooleanStreamReader((XMLStreamReader) source)));
			}
			if (source instanceof XMLEventReader) {
				return create((JAXBElement<?>) u.unmarshal(new BooleanEventReader((XMLEventReader) source)));
			}

			InputSource input;
			XMLReader reader = null;
			if (source instanceof SAXSource) {
				SAXSource sax = (SAXSource) source;
				input = sax.getInputSource();
				reader = sax.getXMLReader();
			} else if (source instanceof InputSource) {
				input = (InputSource) source;
			} else if (source instanceof InputStream) {
				input = new InputSource((InputStream) source);
			} else if (source instanceof byte[]) {
				input = new InputSource(new ByteArrayInputStream((byte[]) source));
			} else if (source instanceof StreamSource) {
				StreamSource stream = (StreamSource) source;
				input = new InputSource(stream.getSystemId());
				input.setPublicId(stream.getPublicId());
				input.setByteStream(stream.getInputStream());
				input.setCharacterStream(stream.getReader());
			} else {
				throw new IllegalArgumentException(String.format("Unsupported source=\"%s\"", source.getClass().getName()));
			}
			// A SAXSource with its own reader retains the caller's parser settings and resolver.
			if (reader == null) {
				reader = secureReader();
			}
			UnmarshallerHandler handler = u.getUnmarshallerHandler();
			BooleanAttributeValidationFilter filter = new BooleanAttributeValidationFilter(handler);
			filter.setParent(reader);
			reader.setContentHandler(filter);
			reader.parse(input);
			return create((JAXBElement<?>) handler.getResult());
		}catch(JAXBException | SAXException | ParserConfigurationException | XMLStreamException e){
			// JAXB and StAX keep linked exceptions outside the usual cause chain.
			for (Throwable cause = e; cause != null;) {
				if (cause instanceof SyntaxException) {
					throw (SyntaxException) cause;
				}
				Throwable linked = cause instanceof JAXBException ? ((JAXBException) cause).getLinkedException()
						: cause instanceof XMLStreamException ? ((XMLStreamException) cause).getNestedException() : null;
				cause = linked != null ? linked : cause.getCause();
			}
			throw new SyntaxException(e);
		}
	}

	private static XMLReader secureReader() throws ParserConfigurationException, SAXException
	{
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
		factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		factory.setXIncludeAware(false);
		XMLReader reader = factory.newSAXParser().getXMLReader();
		reader.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		reader.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
		DefaultHandler2 externalEntities = new DefaultHandler2() {
			@Override
			public void externalEntityDecl(String name, String publicId, String systemId) throws SAXException {
				throw new SAXException("External XML entity declarations are not allowed");
			}

			@Override
			public void startDTD(String name, String publicId, String systemId) throws SAXException {
				if (publicId != null || systemId != null) {
					throw new SAXException("External XML DTDs are not allowed");
				}
			}

			@Override
			public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
				throw new SAXException("External XML entities are not allowed");
			}

			@Override
			public InputSource resolveEntity(String name, String publicId, String baseURI, String systemId) throws SAXException {
				throw new SAXException("External XML entities are not allowed");
			}

			@Override
			public void error(SAXParseException failure) throws SAXException {
				throw failure;
			}

			@Override
			public void fatalError(SAXParseException failure) throws SAXException {
				throw failure;
			}
		};
		// Disabled external entities would otherwise be silently skipped. Reject their declarations instead.
		reader.setProperty("http://xml.org/sax/properties/declaration-handler", externalEntities);
		reader.setProperty("http://xml.org/sax/properties/lexical-handler", externalEntities);
		reader.setEntityResolver(externalEntities);
		reader.setErrorHandler(externalEntities);
		return reader;
	}

	private static void validateDom(Node node) throws SAXException
	{
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			AttributesImpl attributes = new AttributesImpl();
			NamedNodeMap nodes = node.getAttributes();
			for (int i = 0; i < nodes.getLength(); i++) {
				Node attribute = nodes.item(i);
				String localName = attribute.getLocalName() != null ? attribute.getLocalName() : attribute.getNodeName();
				attributes.addAttribute(namespace(attribute.getNamespaceURI()), localName,
						attribute.getNodeName(), "CDATA", attribute.getNodeValue());
			}
			validateAttributes(node.getNamespaceURI(), node.getLocalName(), attributes);
		}
		for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			validateDom(child);
		}
	}

	private static void validateAttributes(String uri, String localName, AttributesImpl attributes) throws SAXException
	{
		new BooleanAttributeValidationFilter(new DefaultHandler()).startElement(namespace(uri), localName, localName, attributes);
	}

	private static String namespace(String uri)
	{
		return uri == null ? "" : uri;
	}

	private static final class BooleanStreamReader extends StreamReaderDelegate
	{
		private BooleanStreamReader(XMLStreamReader parent) throws XMLStreamException {
			super(parent);
			validateCurrent();
		}

		@Override
		public int next() throws XMLStreamException {
			int event = super.next();
			validateCurrent();
			return event;
		}

		@Override
		public int nextTag() throws XMLStreamException {
			int event = super.nextTag();
			validateCurrent();
			return event;
		}

		private void validateCurrent() throws XMLStreamException {
			if (isStartElement()) {
				AttributesImpl attributes = new AttributesImpl();
				for (int i = 0; i < getAttributeCount(); i++) {
					attributes.addAttribute(namespace(getAttributeNamespace(i)), getAttributeLocalName(i),
							getAttributeLocalName(i), "CDATA", getAttributeValue(i));
				}
				try {
					validateAttributes(getNamespaceURI(), getLocalName(), attributes);
				} catch (SAXException failure) {
					throw new XMLStreamException(failure);
				}
			}
		}
	}

	private static final class BooleanEventReader extends EventReaderDelegate
	{
		private BooleanEventReader(XMLEventReader parent) {
			super(parent);
		}

		@Override
		public XMLEvent nextEvent() throws XMLStreamException {
			return validate(super.nextEvent());
		}

		@Override
		public XMLEvent nextTag() throws XMLStreamException {
			return validate(super.nextTag());
		}

		@Override
		public XMLEvent peek() throws XMLStreamException {
			return validate(super.peek());
		}

		private XMLEvent validate(XMLEvent event) throws XMLStreamException {
			if (event != null && event.isStartElement()) {
				StartElement element = event.asStartElement();
				AttributesImpl attributes = new AttributesImpl();
				Iterator<Attribute> iterator = element.getAttributes();
				while (iterator.hasNext()) {
					Attribute attribute = iterator.next();
					attributes.addAttribute(namespace(attribute.getName().getNamespaceURI()),
							attribute.getName().getLocalPart(), attribute.getName().getLocalPart(), "CDATA", attribute.getValue());
				}
				try {
					validateAttributes(element.getName().getNamespaceURI(), element.getName().getLocalPart(), attributes);
				} catch (SAXException failure) {
					throw new XMLStreamException(failure);
				}
			}
			return event;
		}
	}

	protected abstract T create(JAXBElement<?> jaxbInstance) throws SyntaxException;
}
