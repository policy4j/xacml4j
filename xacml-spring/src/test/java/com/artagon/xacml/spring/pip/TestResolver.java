package com.artagon.xacml.spring.pip;

import java.util.Map;

import org.junit.Ignore;
import org.w3c.dom.Node;

import com.artagon.xacml.v30.BagOfAttributeExp;
import com.artagon.xacml.v30.spi.pip.XacmlAttributeDescriptor;
import com.artagon.xacml.v30.spi.pip.XacmlAttributeDesignator;
import com.artagon.xacml.v30.spi.pip.XacmlAttributeResolverDescriptor;
import com.artagon.xacml.v30.spi.pip.XacmlContentResolverDescriptor;

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
