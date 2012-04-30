package com.artagon.xacml.v30;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.artagon.xacml.v30.pdp.AttributeExp;
import com.artagon.xacml.v30.pdp.BagOfAttributeExp;
import com.artagon.xacml.v30.spi.pip.AttributeResolver;
import com.artagon.xacml.v30.spi.pip.AttributeResolverDescriptorBuilder;
import com.artagon.xacml.v30.spi.pip.BaseAttributeResolver;
import com.artagon.xacml.v30.spi.pip.ResolverContext;

public class ExpectedAttributeResolverBuilder
{
	private AttributeResolverDescriptorBuilder b;
	private Map<String, BagOfAttributeExp> values;
	
	private ExpectedAttributeResolverBuilder(AttributeResolverDescriptorBuilder b){
		this.b = b;
		this.values = new HashMap<String, BagOfAttributeExp>();
	}
	
	public static ExpectedAttributeResolverBuilder builder(String id, AttributeCategory category, String issuer){
		return new ExpectedAttributeResolverBuilder(AttributeResolverDescriptorBuilder.builder(id, "ExpectedAttributeResolver " + id, issuer, category));
	}
	
	public static ExpectedAttributeResolverBuilder builder(String id, AttributeCategory category){
		return builder(id, category, null);
	}
	
	public ExpectedAttributeResolverBuilder withAttributeValue(String id, AttributeExp value){
		b.attribute(id, value.getType());
		this.values.put(id, value.toBag());
		return this;
	}
	
	public ExpectedAttributeResolverBuilder withAttributeValue(String id, BagOfAttributeExp value){
		b.attribute(id, value.getDataType());
		this.values.put(id, value);
		return this;
	}
	
	public AttributeResolver build(){
		return new BaseAttributeResolver(b.build()) {
			
			@Override
			protected Map<String, BagOfAttributeExp> doResolve(ResolverContext context)
					throws Exception {
				return Collections.unmodifiableMap(values);
			}
		};
	}
}
