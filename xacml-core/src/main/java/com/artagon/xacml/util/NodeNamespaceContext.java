package com.artagon.xacml.util;

import java.util.Collections;
import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.google.common.base.Preconditions;

public class NodeNamespaceContext implements NamespaceContext
{
	private final static Logger log = LoggerFactory.getLogger(NodeNamespaceContext.class);
	
	private Node node;

	public NodeNamespaceContext(Node node){
		Preconditions.checkNotNull(node);
		this.node = node;
	}
	
	@Override
	public String getNamespaceURI(String prefix){
		 if (XMLConstants.XML_NS_PREFIX.equals(prefix)) {
	            return XMLConstants.XML_NS_URI;
	     }
		 if (XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)) {
	            return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
	    }
		String namespaceURI = node.lookupNamespaceURI(prefix);
		log.debug("NamespaceURI=\"{}\" for prefix=\"{}\"", namespaceURI, prefix);
		return namespaceURI == null?XMLConstants.NULL_NS_URI:namespaceURI;
	}

	@Override
	public String getPrefix(String namespaceURI) {
		String prefix = node.lookupPrefix(namespaceURI);
		log.debug("Namespace prefix=\"{}\" for namespaceURI=\"{}\"", prefix, namespaceURI);
		return prefix;
	}

	@Override
	public Iterator<String> getPrefixes(String namespaceURI) {
		String prefix = getPrefix(namespaceURI);
		return (prefix == null)?
				Collections.<String>emptyList().iterator():
					Collections.singleton(prefix).iterator();
	}
}
