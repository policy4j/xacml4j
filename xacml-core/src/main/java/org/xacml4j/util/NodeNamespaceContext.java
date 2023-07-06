package org.xacml4j.util;

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

import java.util.Collections;
import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.google.common.base.Preconditions;

public final class NodeNamespaceContext implements NamespaceContext
{
	private final static Logger log = LoggerFactory.getLogger(NodeNamespaceContext.class);

	private Node node;

	public NodeNamespaceContext(Node node){
		Preconditions.checkNotNull(node, "node");
		this.node = node;
	}

	@Override
	public String getNamespaceURI(String prefix){
		String namespaceURI = null;
		if (prefix == null || prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
			namespaceURI = node.lookupNamespaceURI(null);
		} else {
			namespaceURI = node.lookupNamespaceURI(prefix);
		}
		if(log.isDebugEnabled()){
			log.debug("Namespace prefix=\"{}\" " +
					          "for namespaceURI=\"{}\"", prefix, namespaceURI);
		}
		return namespaceURI;
	}

	@Override
	public String getPrefix(String namespaceURI) {
		String prefix = node.lookupPrefix(namespaceURI);
		if(log.isDebugEnabled()){
			log.debug("Namespace prefix=\"{}\" " +
					"for namespaceURI=\"{}\"", prefix, namespaceURI);
		}
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
