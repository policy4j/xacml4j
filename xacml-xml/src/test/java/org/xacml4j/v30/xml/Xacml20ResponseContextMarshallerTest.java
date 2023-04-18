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

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.marshal.Marshaller;
import org.xacml4j.v30.marshal.Unmarshaller;
import org.xacml4j.v30.RequestContext;

import com.google.common.base.Supplier;
import com.google.common.io.Closeables;


public class Xacml20ResponseContextMarshallerTest {

	private Marshaller<ResponseContext> responseMarshaller;
	private Unmarshaller<RequestContext> requestUnmarshaller;

	@Before
	public void SetUp() throws Exception
	{
		responseMarshaller = new Xacml20ResponseContextMarshaller();
		requestUnmarshaller = new Xacml20RequestContextUnmarshaller();
	}


	@Test
	@SuppressWarnings("unchecked")
	public void testRequestMarshaller() throws Exception
	{
		RequestContext request0 = getRequest("MarshallerTestRequest.xml");
		RequestContext request1 = getRequest("001A-Request.xml");
		RequestContext request2 = getRequest("IIA013Request.xml");
		RequestContext request3 = getRequest("IIA013Request.xml");

	}

	private static Supplier<InputStream> getPolicy() {
		String path = "MarshallerTestPolicy.xml";
		return getClasspathResource(path);
	}

	public static Supplier<InputStream> getClasspathResource(final String resourcePath) {
		return new Supplier<InputStream>() {
			@Override
			public InputStream get() {
				ClassLoader cl = Thread.currentThread().getContextClassLoader();
				return cl.getResourceAsStream(resourcePath);
			}
		};
	}

	public RequestContext getRequest(String resourcePath) throws Exception {
		InputStream in = null;
		try {
			in = getClasspathResource(resourcePath).get();
			return requestUnmarshaller.unmarshal(in);
		} finally {
			Closeables.closeQuietly(in);
		}
	}
	
}
