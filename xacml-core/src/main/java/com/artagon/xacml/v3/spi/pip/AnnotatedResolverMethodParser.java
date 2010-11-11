package com.artagon.xacml.v3.spi.pip;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.artagon.xacml.v3.AttributeCategories;
import com.artagon.xacml.v3.AttributeDesignatorKey;
import com.artagon.xacml.v3.AttributeReferenceKey;
import com.artagon.xacml.v3.AttributeSelectorKey;
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
		AttributeResolverDescriptorBuilder b = AttributeResolverDescriptorBuilder.create(
				d.id(), d.name(), 
				d.issuer(),
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
		b.keys(parseRequestKeyReferences(m));
		if(!m.getReturnType().isAssignableFrom(Map.class)){
			throw new XacmlSyntaxException(
					"Attribute resolver method=\"%s\" " +
					"must return=\"%s\"", m.getName(), 
					Map.class.getName());
		}
		return b.build();
	}
	
	private List<AttributeReferenceKey> parseRequestKeyReferences(Method m) 
		throws XacmlSyntaxException
	{
		List<AttributeReferenceKey> keys = new LinkedList<AttributeReferenceKey>();
		Class<?>[] types = m.getParameterTypes();
		int  i = 0;
		for(Annotation[] p : m.getParameterAnnotations())
		{
			if(p.length == 0){
				if(i == 0 && 
						(types[i].isInstance(PolicyInformationPointContext.class))){
					continue;
				}
				throw new XacmlSyntaxException(
						"Resolver parameter without annotiation must be of type=\"%s\"", 
						PolicyInformationPointContext.class);
			}
			if(p.length > 0 && p[0] instanceof XacmlAttributeDesignator)
			{
				XacmlAttributeDesignator ref = (XacmlAttributeDesignator)p[0];
				keys.add(new AttributeDesignatorKey(
							AttributeCategories.parse(ref.category()), 
							ref.attributeId(), 
							XacmlDataTypesRegistry.getType(ref.dataType()), 
							Strings.emptyToNull(ref.issuer())));
				continue;
			}
			if(p.length > 0 && p[0] instanceof XacmlAttributeSelector)
			{
					XacmlAttributeSelector ref = (XacmlAttributeSelector)p[0];
					keys.add(new AttributeSelectorKey(
							AttributeCategories.parse(ref.category()), 
							ref.xpath(), 
							XacmlDataTypesRegistry.getType(ref.dataType()), 
							Strings.emptyToNull(ref.contextAttributeId())));
					continue;
			}
			i++;
			throw new XacmlSyntaxException(
						"Unknown annotation of type=\"%s\" found", 
						types[0].getClass());
		}
		return keys;
	}
}
