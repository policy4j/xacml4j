package org.xacml4j.v30;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.xacml4j.v30.spi.pip.AttributeResolver;
import org.xacml4j.v30.spi.pip.AttributeResolverDescriptorBuilder;
import org.xacml4j.v30.spi.pip.BaseAttributeResolver;
import org.xacml4j.v30.spi.pip.ResolverContext;


public class ExpectedAttributeResolverBuilder
{
	private AttributeResolverDescriptorBuilder b;
	private Map<String, BagOfAttributeExp> values;

	private ExpectedAttributeResolverBuilder(AttributeResolverDescriptorBuilder b){
		this.b = b;
		this.values = new HashMap<String, BagOfAttributeExp>();
	}

	public static ExpectedAttributeResolverBuilder builder(String id, CategoryId category, String issuer){
		return new ExpectedAttributeResolverBuilder(AttributeResolverDescriptorBuilder.builder(id, "ExpectedAttributeResolver " + id, issuer, category));
	}

	public static ExpectedAttributeResolverBuilder builder(String id, CategoryId category){
		return builder(id, category, null);
	}

	public ExpectedAttributeResolverBuilder withDesignatorKeyRef(
			CategoryId category, String attributeId, AttributeExpType type)
	{
		b.requestContextKey(category, attributeId, type);
		return this;
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
