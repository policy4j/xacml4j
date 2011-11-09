package com.artagon.xacml.v20;

import java.util.HashMap;
import java.util.Map;

import com.artagon.xacml.v30.pdp.BagOfAttributeExp;
import com.artagon.xacml.v30.spi.pip.XacmlAttributeDescriptor;
import com.artagon.xacml.v30.spi.pip.XacmlAttributeDesignator;
import com.artagon.xacml.v30.spi.pip.XacmlAttributeResolverDescriptor;
import com.artagon.xacml.v30.types.StringType;
import com.artagon.xacml.v30.types.StringExp;

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
					StringType.STRING.bagOf(StringType.STRING.create("Physician")));
		}
		return attributes;
	}
}
