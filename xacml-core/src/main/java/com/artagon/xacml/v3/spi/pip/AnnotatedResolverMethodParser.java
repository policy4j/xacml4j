package com.artagon.xacml.v3.spi.pip;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.artagon.xacml.v3.AttributeCategories;
import com.artagon.xacml.v3.XacmlSyntaxException;
import com.artagon.xacml.v3.marshall.XacmlDataTypesRegistry;
import com.artagon.xacml.v3.sdk.XacmlAttributeDescriptor;
import com.artagon.xacml.v3.sdk.XacmlAttributeDesignator;
import com.artagon.xacml.v3.sdk.XacmlAttributeResolverDescriptor;
import com.artagon.xacml.v3.sdk.XacmlAttributeSelector;
import com.google.common.base.Strings;

public class AnnotatedResolverMethodParser 
{
	
	public AttributeResolverDescriptor parse(Method m) 
		throws XacmlSyntaxException
	{
		XacmlAttributeResolverDescriptor d = m.getAnnotation(XacmlAttributeResolverDescriptor.class);
		AttributeResolverDescriptorBuilder b = AttributeResolverDescriptorBuilder.create(d.id(), d.name(), 
				AttributeCategories.parse(d.category()));
		b.cache(d.cacheTTL());
		XacmlAttributeDescriptor[] attributes = d.attributes();
		if(attributes == null || 
				attributes.length == 0){
			throw new XacmlSyntaxException("At least attribute " +
					"must be specified by the descriptor on method=\"{}\"", m.getName());
		}
		for(XacmlAttributeDescriptor attr : attributes){
			b.attribute(attr.id(), 
					XacmlDataTypesRegistry.getType(attr.dataType()));
		}
		Class<?>[] types = m.getParameterTypes();
		int  i = 0;
		for(Annotation[] p : m.getParameterAnnotations())
		{
			if(p == null || 
					(p.length == 0)){
				if(types)
			}
				if(p[0] instanceof XacmlAttributeDesignator){
					XacmlAttributeDesignator ref = (XacmlAttributeDesignator)p[0];
					b.designatorRef(
							AttributeCategories.parse(ref.category()), 
							ref.attributeId(), 
							XacmlDataTypesRegistry.getType(ref.dataType()), 
							Strings.emptyToNull(ref.issuer()));
					continue;
				}
				if(p[0] instanceof XacmlAttributeSelector){
					XacmlAttributeSelector ref = (XacmlAttributeSelector)p[0];
					b.selectorRef(
							AttributeCategories.parse(ref.category()), 
							ref.xpath(), 
							XacmlDataTypesRegistry.getType(ref.dataType()), 
							Strings.emptyToNull(ref.contextAttributeId()));
					continue;
				}
				throw new XacmlSyntaxException(
						"Unknown annotation of type=\"%s\" found", p[0].getClass());
			}
		}
		return b.build();
	}
}
