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

import java.util.Optional;

import javax.xml.bind.JAXBContext;

import org.oasis.xacml.v30.jaxb.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.xacml4j.v30.Content;

import com.google.common.base.Preconditions;

public class JAXBUtils
{
	private static final Logger LOG = LoggerFactory.getLogger(JAXBUtils.class);
	private static final JAXBContext INSTANCE;
	private static final char SEP = ':';
	static{
			try{
			INSTANCE = JAXBContext.newInstance(
					org.oasis.xacml.v30.jaxb.ObjectFactory.class.getPackage().getName() +
					SEP +
					org.oasis.xacml.v20.jaxb.policy.ObjectFactory.class.getPackage().getName() +
					SEP +
					org.oasis.xacml.v20.jaxb.context.ObjectFactory.class.getPackage().getName());
		}catch(Exception e){
				LOG.error(e.getMessage(), e);
			throw new IllegalStateException("Failed to initialize JAXB context", e);
		}
	}

	/** Private constructor for utility class */
	private JAXBUtils() {}

	public static JAXBContext getInstance()
	{
		Preconditions.checkState(INSTANCE != null, "Failed to initialize JAXB context");
		return INSTANCE;
	}

	public static Optional<Content> from(ContentType c)
	{
		if(c != null && c.getContent().size() > 0){
			Node node = (Node)(c.getContent().get(0));
			if(node.getNodeType() == Node.ELEMENT_NODE){
				return Content.fromNode(node, Content.Type.XML_UTF8);
			}
			if(node.getNodeType() == Node.CDATA_SECTION_NODE){
				return Content.fromString(node.getTextContent());
			}
		}
		return Optional.empty();
	}

	public static Optional<ContentType> from(Optional<Content> c){
		return c.map(v->{
			ContentType contentNode = new ContentType();
			if(v.getType() == Content.Type.XML_UTF8){
				// JAXB is going to use DOM
				contentNode.getContent().add(v.toNode());
				return contentNode;
			}else{
				// JAXB is going to  create CDATA for content
				contentNode.getContent().add(v.asString());
			}
			return contentNode;
		});
	}
}
