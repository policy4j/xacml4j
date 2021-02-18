package org.xacml4j.v30.pdp;

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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Entity;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.spi.xpath.XPathProvider;
import org.xacml4j.v30.types.TypeToString;
import org.xacml4j.v30.types.XPathExp;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

class DefaultEvaluationContextHandler
	implements EvaluationContextHandler
{
	private final static Logger log = LoggerFactory.getLogger(DefaultEvaluationContextHandler.class);

	private final static String CONTENT_SELECTOR = "urn:oasis:names:tc:xacml:3.0:content-selector";

	private XPathProvider xpathProvider;
	private PolicyInformationPoint pip;

	private RequestContextCallback requestCallback;

	private Map<CategoryId, Node> contentCache;

	private Stack<AttributeDesignatorKey> designatorResolutionStack;
	private Stack<AttributeSelectorKey> selectorResolutionStack;
	private Stack<CategoryId> contentResolutionStack;

	DefaultEvaluationContextHandler(
			RequestContextCallback requestCallback,
			XPathProvider xpathProvider,
			PolicyInformationPoint pip)
	{
		Preconditions.checkNotNull(xpathProvider);
		Preconditions.checkNotNull(pip);
		Preconditions.checkNotNull(requestCallback);

		this.xpathProvider = xpathProvider;
		this.pip = pip;
		this.requestCallback = requestCallback;
		this.contentCache = new HashMap<CategoryId, Node>();
		this.selectorResolutionStack = new Stack<AttributeSelectorKey>();
		this.designatorResolutionStack = new Stack<AttributeDesignatorKey>();
		this.contentResolutionStack = new Stack<CategoryId>();
	}

	@Override
	public BagOfAttributeExp resolve(
			EvaluationContext context,
			AttributeDesignatorKey key)
		throws EvaluationException
	{

		Preconditions.checkNotNull(context);
		Preconditions.checkNotNull(key);
		final Entity entity = requestCallback.getEntity(key.getCategory());
		if (entity != null) {
			Collection<Attribute> attrs = entity.getAttributes(
					key.getAttributeId(), key.getIssuer());
			if (!attrs.isEmpty()) {
				final ImmutableList.Builder<AttributeExp> b = ImmutableList.builder();
				for (Attribute a : attrs) {
					b.addAll(a.getValuesByType(key.getDataType()));
				}
				final BagOfAttributeExp v = key.getDataType().bagOf(b.build());
				if (log.isDebugEnabled()) {
					log.debug("Resolved designator=\"{}\" from request to value=\"{}\"",
					          key, v);
				}
				return v;
			}
		}

		Preconditions.checkState(
				!designatorResolutionStack.contains(key),
				"Cyclic designator=\"%s\" resolution detected", key);
		try {
			designatorResolutionStack.push(key);
			final BagOfAttributeExp v = pip.resolve(context, key);
			if (log.isDebugEnabled()) {
				log.debug("Resolved designator=\"{}\" from PIP to value=\"{}\"",
				          key, v);
			}
			return v;
		} catch (Exception e) {
			if (log.isDebugEnabled()) {
				log.debug(e.getMessage(), e);
			}
			throw new AttributeReferenceEvaluationException(key);
		} finally {
			designatorResolutionStack.pop();
		}
	}

	@Override
	public BagOfAttributeExp resolve(
			EvaluationContext context,
			AttributeSelectorKey ref)
			throws EvaluationException
	{
		Preconditions.checkState(
				!selectorResolutionStack.contains(ref),
				"Cyclic designator=\"%s\" resolution detected", ref);
		try
		{
			selectorResolutionStack.push(ref);
			BagOfAttributeExp v =  doResolve(context, ref);
			if(log.isDebugEnabled()){
				log.debug("Resolved " +
						"selector=\"{}\" to bag=\"{}\"", ref, v);
			}
			return v;
		}finally{
			selectorResolutionStack.pop();
		}
	}

	/**
	 * Resolves category content via {@link PolicyInformationPoint}
	 *
	 * @param context an evaluation context
	 * @param category an attribute category
	 * @return {@link BagOfAttributeExp}
	 * @exception Exception if content can not be retrieved
	 */
	private Node doGetContent(EvaluationContext context, CategoryId category)
		throws Exception
	{
		Node content;
		if(contentCache.containsKey(category)){
			content = contentCache.get(category);
			if(log.isDebugEnabled()){
				log.debug("Resolved content=\"{}\" " +
						"from cache", content);
			}
			return content;
		}
		Preconditions.checkState(
				!contentResolutionStack.contains(category));
		try
		{
			contentResolutionStack.push(category);
			content = pip.resolve(context, category);
			if(log.isDebugEnabled()){
				log.debug("Resolved content=\"{}\" " +
						"from PIP", content);
			}
			contentCache.put(category, content);
			return content;
		}finally{
			contentResolutionStack.pop();
		}
	}

	private BagOfAttributeExp doResolve(
			EvaluationContext context,
			AttributeSelectorKey ref) throws EvaluationException
	{
		try
		{
			Entity entity = requestCallback.getEntity(ref.getCategory());
			Node content = (entity != null)?entity.getContent():null;
			if(content == null){
				content = doGetContent(context, ref.getCategory());
			}
			Node contextNode = content;
			Collection<AttributeExp> v = entity.getAttributeValues(
						(ref.getContextSelectorId() == null?CONTENT_SELECTOR:ref.getContextSelectorId()),
								XacmlTypes.XPATH);
			if(v.size() > 1){
				throw new AttributeReferenceEvaluationException(ref,
						"Found more than one value of=\"%s\"", ref.getContextSelectorId());
			}
			if(v.size() == 1){
				XPathExp xpath = (XPathExp)v.iterator().next();
				if(xpath.getCategory() != ref.getCategory()){
					throw new AttributeReferenceEvaluationException(ref,
							"AttributeSelector category=\"%s\" and " +
							"ContextAttributeId XPathExpression " +
							"category=\"%s\" do not match", ref.getCategory(),
							xpath.getCategory());
				}
				if(log.isDebugEnabled()){
					log.debug("Evaluating " +
							"contextSelector xpath=\"{}\"", xpath.getValue());
				}
				contextNode = xpathProvider.evaluateToNode(xpath.getPath(), content);
			}
			NodeList nodeSet = xpathProvider.evaluateToNodeSet(ref.getPath(), contextNode);
			if(nodeSet == null ||
					nodeSet.getLength() == 0){
				log.debug("Selected nodeset via xpath=\"{}\" and category=\"{}\" is empty",
						ref.getPath(), ref.getCategory());
				return ref.getDataType().bagType().createEmpty();
			}
			if(log.isDebugEnabled()){
				log.debug("Found=\"{}\" nodes via xpath=\"{}\" and category=\"{}\"",
						new Object[]{nodeSet.getLength(), ref.getPath(), ref.getCategory()});
			}
			return toBag(context, ref, nodeSet);
		}
		catch(org.xacml4j.v30.spi.xpath.XPathEvaluationException e){
			if(log.isDebugEnabled()){
				log.debug(e.getMessage(), e);
			}
			context.setEvaluationStatus(Status.processingError().build());
			throw new AttributeReferenceEvaluationException(Status.processingError().build(), ref, e.getMessage());
		}
		catch(EvaluationException e){
			if(log.isDebugEnabled()){
				log.debug(e.getMessage(), e);
			}
			context.setEvaluationStatus(e.getStatus());
			throw e;
		}
		catch(Exception e){
			if(log.isDebugEnabled()){
				log.debug(e.getMessage(), e);
			}
			throw new AttributeReferenceEvaluationException(ref);
		}
	}

	/**
	 * Converts a given node list to the {@link BagOfAttributeExp}
	 *
	 * @param context an evaluation context
	 * @param ref an attribute selector
	 * @param nodeSet a node set
	 * @return {@link BagOfAttributeExp}
	 * @throws EvaluationException if node list conversion fails
	 */
	private BagOfAttributeExp toBag(
			EvaluationContext context,
			AttributeSelectorKey ref, NodeList nodeSet)
		throws EvaluationException
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
					throw new AttributeReferenceEvaluationException(ref,
							"Unsupported DOM node type=\"%d\"", n.getNodeType());
			}
			try
			{
				Optional<TypeToString> toString = TypeToString.Types.getIndex().get(ref.getDataType());
				if(!toString.isPresent()){
					throw new AttributeReferenceEvaluationException(ref,
							"Unsupported XACML type=\"%d\"",
							ref.getDataType().getDataTypeId());
				}
				AttributeExp value = toString.get().fromString(v);
				if(log.isDebugEnabled()){
					log.debug("Node of type=\"{}\" " +
							"converted attribute=\"{}\"", n.getNodeType(), value);
				}
				values.add(value);
			}catch(Exception e){
				throw new AttributeReferenceEvaluationException(ref);
			}
		}
	  	return ref.getDataType().bagType().create(values);
	}

	@Override
	public Node evaluateToNode(EvaluationContext context, XPathExp xpath)
			throws XPathEvaluationException
	{
		try{

			return xpathProvider.evaluateToNode(xpath.getPath(),
					new ContentSupplier().getContent(context, xpath));
		}catch(org.xacml4j.v30.spi.xpath.XPathEvaluationException e){
			throw new XPathEvaluationException(xpath.getPath(), e);
		}
	}

	@Override
	public NodeList evaluateToNodeSet(EvaluationContext context, XPathExp xpath)
			throws EvaluationException {
		try{
			return xpathProvider.evaluateToNodeSet(xpath.getPath(), new ContentSupplier().getContent(context, xpath));
		}catch(org.xacml4j.v30.spi.xpath.XPathEvaluationException e){
			throw new XPathEvaluationException(xpath.getPath(), e);
		}
	}

	@Override
	public Number evaluateToNumber(EvaluationContext context, XPathExp xpath) throws EvaluationException {
		try{
			return xpathProvider.evaluateToNumber(xpath.getPath(), new ContentSupplier().getContent(context, xpath));
		}catch(org.xacml4j.v30.spi.xpath.XPathEvaluationException e){
			throw new XPathEvaluationException(xpath.getPath(), e);
		}
	}

	@Override
	public String evaluateToString(EvaluationContext context, XPathExp xpath)
			throws XPathEvaluationException {
		try{
			return xpathProvider.evaluateToString(xpath.getPath(), new ContentSupplier().getContent(context, xpath));
		}catch(org.xacml4j.v30.spi.xpath.XPathEvaluationException e){
			throw new XPathEvaluationException(xpath.getPath(), e);
		}
	}

	private class ContentSupplier
	{
		public Node getContent(EvaluationContext context, XPathExp xpath) throws XPathEvaluationException
		{
			Entity entity = requestCallback.getEntity(xpath.getCategory());
			Node content = (entity != null)?entity.getContent():null;
			try{
				if(content == null){
					content = doGetContent(context, xpath.getCategory());
				}
			}catch(Exception e){
				throw new XPathEvaluationException(xpath.getPath(), e);
			}
			if(content == null){
				throw new XPathEvaluationException(xpath.getPath(),
							"Not content found for category=\"%s\"", xpath.getCategory());
			}
			return content;
		}
	}
}
