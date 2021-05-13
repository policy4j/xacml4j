package org.xacml4j.v20;

/*
 * #%L
 * Xacml4J Conformance Tests
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

import java.util.HashMap;
import java.util.Map;

import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.spi.pip.XacmlAttributeDescriptor;
import org.xacml4j.v30.spi.pip.XacmlAttributeDesignator;
import org.xacml4j.v30.spi.pip.XacmlAttributeResolverDescriptor;
import org.xacml4j.v30.types.StringExp;


public class Xacml20ConformanceAttributeResolver
{
	@XacmlAttributeResolverDescriptor(
			id="urn:oasis:names:tc:xacml:2.0:conformance-test:IIA002:policy",
			name="OASIS XACML Conformance Test Suite Resolver for test IIA002",
			category="urn:oasis:names:tc:xacml:1.0:subject-category:access-subject",
			attributes={
				@XacmlAttributeDescriptor(dataType="http://www.w3.org/2001/XMLSchema#string",
						id="urn:oasis:names:tc:xacml:1.0:example:attribute:role")
	})
	public Map<String, BagOfAttributeExp> IIA002(
			@XacmlAttributeDesignator(
					category="urn:oasis:names:tc:xacml:1.0:subject-category:access-subject",
					attributeId="urn:oasis:names:tc:xacml:1.0:subject:subject-id",
					dataType="http://www.w3.org/2001/XMLSchema#string") BagOfAttributeExp subjectId)
	{
		StringExp name = BagOfAttributeExp.value(subjectId);
		Map<String, BagOfAttributeExp> attributes = new HashMap<String, BagOfAttributeExp>();
		if(name.getValue().equalsIgnoreCase("Julius Hibbert")){
			attributes.put("urn:oasis:names:tc:xacml:1.0:example:attribute:role",
					StringExp.valueOf("Physician").toBag());
		}
		return attributes;
	}
}
