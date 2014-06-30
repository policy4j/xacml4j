package org.xacml4j.v30;

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


import java.util.Collection;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xacml4j.util.DOMUtil;
import org.xacml4j.v30.pdp.XPathEvaluationException;
import org.xacml4j.v30.spi.xpath.XPathProvider;
import org.xacml4j.v30.types.TypeToString;
import org.xacml4j.v30.types.XPathExp;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;

/**
 * An entity represents a collection of related attributes
 *
 * @author Giedrius Trumpickas
 */
public final class Entity extends AttributeContainer
{
	private final static String CONTENT_SELECTOR = "urn:oasis:names:tc:xacml:3.0:content-selector";

	private final static Logger log = LoggerFactory.getLogger(Entity.class);

	private final Document content;

	private Entity(Builder b) {
		super(b);
		this.content = DOMUtil.copyNode(b.content);
	}

	public static Builder builder(){
		return new Builder();
	}

	/**
	 * Gets entity with all include
	 * in result attributes
	 * @return {@link Entity} with all include in result attributes
	 */
	public Entity getIncludeInResult(){
		return Entity
		.builder()
		.copyOf(this, new Predicate<Attribute>() {
			public boolean apply(Attribute a){
				return a.isIncludeInResult();
			}
		}).build();
	}

	/**
	 * Gets content as {@link Node}
	 * instance
	 *
	 * @return a {@link Node} instance or {@code null}
	 */
	public Node getContent(){
		return content;
	}

	/**
	 * Tests if this entity has content
	 *
	 * @return {@code true} if entity has content; returns {@code false} otherwise
	 */
	public boolean hasContent(){
		return content != null;
	}

	public BagOfAttributeExp getAttributeValues(
			String xpath,
			XPathProvider xpathProvider,
			AttributeExpType type,
			String contextSelectorId)
					throws XPathEvaluationException
	{
		try
		{
			Node contextNode = content;
			Collection<AttributeExp> v = getAttributeValues(
						(contextSelectorId == null?CONTENT_SELECTOR:contextSelectorId),
								XacmlTypes.XPATH);
			if(v.size() > 1){
				throw new XPathEvaluationException(xpath,
						Status.syntaxError().build(),
						"Found more than one value of=\"%s\"",
						contextSelectorId);
			}
			if(v.size() == 1){
				XPathExp xpathAttr = (XPathExp)v.iterator().next();
				if(log.isDebugEnabled()){
					log.debug("Evaluating " +
							"contextSelector xpath=\"{}\"", xpathAttr.getValue());
				}
				contextNode = xpathProvider.evaluateToNode(xpathAttr.getPath(), content);
			}
			NodeList nodeSet = xpathProvider.evaluateToNodeSet(xpath, contextNode);
			if(nodeSet == null ||
					nodeSet.getLength() == 0){
				return type.bagType().createEmpty();
			}
			if(log.isDebugEnabled()){
				log.debug("Found=\"{}\" nodes via xpath=\"{}\"",
						new Object[]{nodeSet.getLength(), xpath});
			}
			return toBag(xpath, type, nodeSet);
		}
		catch(EvaluationException e){
			if(log.isDebugEnabled()){
				log.debug(e.getMessage(), e);
			}
			throw e;
		}
		catch(Exception e){
			if(log.isDebugEnabled()){
				log.debug(e.getMessage(), e);
			}
			throw new XPathEvaluationException(xpath, e, e.getMessage());
		}
	}

	/**
	 * Converts a given node list to the {@link BagOfAttributeExp}
	 *
	 * @param xpath XPath for nodes
	 * @param type attribute type
	 * @param nodeSet a node set
	 * @return {@link BagOfAttributeExp}
	 * @throws EvaluationException
	 */
	private BagOfAttributeExp toBag(String xpath,
			AttributeExpType type, NodeList nodeSet)
		throws XPathEvaluationException
	{
		Collection<AttributeExp> values = new LinkedList<AttributeExp>();
		for(int i = 0; i< nodeSet.getLength(); i++)
		{
			Node n = nodeSet.item(i);
			String v = null;
			switch(n.getNodeType()){
				case Node.TEXT_NODE:
					v = ((Text)n).getData();
					break;
				case Node.PROCESSING_INSTRUCTION_NODE:
					v = ((ProcessingInstruction)n).getData();
					break;
				case Node.ATTRIBUTE_NODE:
					v = ((Attr)n).getValue();
					break;
				case Node.COMMENT_NODE:
					v = ((Comment)n).getData();
					break;
				default:
					throw new XPathEvaluationException(
							xpath,
							Status.syntaxError().build(),
							"Unsupported DOM node type=\"%d\"",
							n.getNodeType());
			}
			try
			{
				Optional<TypeToString> toString = TypeToString.Types.getIndex().get(type);
				if(!toString.isPresent()){
					throw new XPathEvaluationException(
							xpath,
							Status.syntaxError().build(),
							"Unsupported XACML type=\"%d\"",
							type.getDataTypeId());
				}
				AttributeExp value = toString.get().fromString(v);
				if(log.isDebugEnabled()){
					log.debug("Node of type=\"{}\" " +
							"converted attribute=\"{}\"", n.getNodeType(), value);
				}
				values.add(value);
			}catch(EvaluationException e){
				throw e;
			}catch(Exception e){
				throw new XPathEvaluationException(xpath,
						Status.processingError().build(),
						e, e.getMessage());
			}
		}
	  	return type.bagType().create(values);
	}



	public BagOfAttributeExp getAttributeValues(String attributeId, AttributeExpType type, String issuer){
		Collection<AttributeExp> values = getAttributeValues(attributeId, issuer, type);
		return type.bagOf(values);
	}

	@Override
	public String toString(){
		return Objects.toStringHelper(this)
		.add("attributes", attributes)
		.add("content", (content != null)?DOMUtil.toString(content.getDocumentElement()):content)
		.toString();
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(attributes, content);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof Entity)) {
			return false;
		}
		Entity a = (Entity) o;
		return Objects.equal(attributes, a.attributes) &&
				DOMUtil.isEqual(content, a.content);
	}

	public static class Builder
		extends AttributeContainer.Builder<Builder>
	{
		private Node content;

		public Builder content(Node node) {
			this.content = DOMUtil.copyNode(node);
			return this;
		}

		public Builder copyOf(Entity a){
			return copyOf(a, Predicates.<Attribute>alwaysTrue());
		}

		public Builder copyOf(Entity a,
				Predicate<Attribute> f){
			Preconditions.checkNotNull(a);
			content(a.getContent());
			attributes(Collections2.filter(a.getAttributes(), f));
			return this;
		}

		@Override
		protected Builder getThis() {
			return this;
		}

		public Entity build(){
			return new Entity(this);
		}
	}
}
