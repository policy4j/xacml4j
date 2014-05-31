package org.xacml4j.spring.pip;

/*
 * #%L
 * Xacml4J Spring 3.x Support Module
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

import java.util.Map;

import org.junit.Ignore;
import org.w3c.dom.Node;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.spi.pip.XacmlAttributeDescriptor;
import org.xacml4j.v30.spi.pip.XacmlAttributeDesignator;
import org.xacml4j.v30.spi.pip.XacmlAttributeResolverDescriptor;
import org.xacml4j.v30.spi.pip.XacmlContentResolverDescriptor;


@Ignore
public class TestResolver
{
	@XacmlAttributeResolverDescriptor(id="testId", name="Test", category="subject", issuer="issuer", cacheTTL=30,
			attributes={
				@XacmlAttributeDescriptor(dataType="http://www.w3.org/2001/XMLSchema#integer", id="testId1"),
				@XacmlAttributeDescriptor(dataType="http://www.w3.org/2001/XMLSchema#boolean", id="testId2"),
				@XacmlAttributeDescriptor(dataType="http://www.w3.org/2001/XMLSchema#string", id="testId3"),
				@XacmlAttributeDescriptor(dataType="http://www.w3.org/2001/XMLSchema#double", id="testId4")
	})
	public Map<String, BagOfAttributeExp> resolveAttributes1(
			@XacmlAttributeDesignator(category="test", attributeId="attr1",
					dataType="http://www.w3.org/2001/XMLSchema#boolean") BagOfAttributeExp k1,
			@XacmlAttributeDesignator(category="test", attributeId="attr2",
					dataType="http://www.w3.org/2001/XMLSchema#integer", issuer="test") BagOfAttributeExp k2)
	{
		return null;
	}

	@XacmlContentResolverDescriptor(id="testId", name="Test", category="subject", cacheTTL=30)
	public Node resolveContent1(@XacmlAttributeDesignator(category="test", attributeId="aaaTTr",
			dataType="http://www.w3.org/2001/XMLSchema#boolean") BagOfAttributeExp k1)
	{
		return null;
	}
}
