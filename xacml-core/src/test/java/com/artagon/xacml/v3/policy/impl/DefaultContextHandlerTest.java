package com.artagon.xacml.v3.policy.impl;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.policy.ContextHandler;
import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.spi.xpath.JDKXPathProvider;

public class DefaultContextHandlerTest
{
	private EvaluationContext context;
	private Request request;
	
	@Before
	public void init(){
		this.context = createStrictMock(EvaluationContext.class);
		this.request = createStrictMock(Request.class);
	}
	
	@Test
	public void testGetContentWithCategoryContextIsAvailable()
	{
		Attributes attributes = createStrictMock(Attributes.class);
		Node content1 = createStrictMock(Node.class);
		expect(request.hasRepeatingCategories()).andReturn(false);
		expect(request.getAttributes(AttributeCategoryId.RESOURCE)).andReturn(Collections.singletonList(attributes));
		expect(attributes.getContent()).andReturn(content1);
		replay(request, attributes);
		ContextHandler handler = new DefaultContextHandler(new JDKXPathProvider(), request);
		Node content2 = handler.getContent(context, AttributeCategoryId.RESOURCE);
		assertSame(content1, content2);
		verify(request, attributes);
	}
	
	@Test
	public void testGetContentWithCategoryContextIsNull()
	{
		Attributes attributes = createStrictMock(Attributes.class);
		expect(request.hasRepeatingCategories()).andReturn(false);
		expect(request.getAttributes(AttributeCategoryId.RESOURCE)).andReturn(Collections.singletonList(attributes));
		expect(attributes.getContent()).andReturn(null);
		replay(request, attributes);
		ContextHandler handler = new DefaultContextHandler(new JDKXPathProvider(), request);
		Node content2 = handler.getContent(context, AttributeCategoryId.RESOURCE);
		assertNull(content2);
		verify(request, attributes);
	}
	
}
