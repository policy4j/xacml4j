package com.artagon.xacml.v20;

import java.util.HashMap;
import java.util.Map;

import com.artagon.xacml.v30.BagOfAttributesExp;
import com.artagon.xacml.v30.spi.pip.XacmlAttributeDescriptor;
import com.artagon.xacml.v30.spi.pip.XacmlAttributeDesignator;
import com.artagon.xacml.v30.spi.pip.XacmlAttributeResolverDescriptor;
import com.artagon.xacml.v30.types.StringType;
import com.artagon.xacml.v30.types.StringValueExp;

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
	public Map<String, BagOfAttributesExp> IIA002(
			@XacmlAttributeDesignator(
					category="urn:oasis:names:tc:xacml:1.0:subject-category:access-subject", 
					attributeId="urn:oasis:names:tc:xacml:1.0:subject:subject-id", 
					dataType="http://www.w3.org/2001/XMLSchema#string") BagOfAttributesExp subjectId)
	{
		StringValueExp name = BagOfAttributesExp.value(subjectId);
		Map<String, BagOfAttributesExp> attributes = new HashMap<String, BagOfAttributesExp>();
		if(name.getValue().equalsIgnoreCase("Julius Hibbert")){
			attributes.put("urn:oasis:names:tc:xacml:1.0:example:attribute:role", 
					StringType.STRING.bagOf(StringType.STRING.create("Physician")));
		}
		return attributes;
	}
}
