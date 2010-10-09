package com.artagon.xacml.v3.spi.pip;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.util.Reflections;
import com.artagon.xacml.v3.AttributeCategories;
import com.artagon.xacml.v3.AttributeCategory;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.RequestContextCallback;
import com.artagon.xacml.v3.ValueExpression;
import com.artagon.xacml.v3.XacmlSyntaxException;
import com.artagon.xacml.v3.marshall.XacmlDataTypesRegistry;
import com.artagon.xacml.v3.sdk.XacmlAttributeCategory;
import com.artagon.xacml.v3.sdk.XacmlAttributeDescriptor;
import com.artagon.xacml.v3.sdk.XacmlAttributeIssuer;
import com.artagon.xacml.v3.sdk.XacmlAttributeKey;
import com.artagon.xacml.v3.sdk.XacmlAttributeResolverDescriptor;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

public class AnnotatedAttributeResolver extends BaseAttributeResolver
{
	private final static Logger log = LoggerFactory.getLogger(AnnotatedAttributeResolver.class);
	private Object instance;
	private Map<AttributeCategory, Map<String, Method>> resolvers;
	
	AnnotatedAttributeResolver(
			AttributeResolverDescriptor descriptor, 
			Map<AttributeCategory, Map<String, Method>> methods, Object instance) 
	{
		super(descriptor);
		Preconditions.checkArgument(instance != null);
		this.instance = instance;
		this.resolvers = new HashMap<AttributeCategory, Map<String,Method>>();
		for(AttributeCategory c : methods.keySet()){
			resolvers.put(c, ImmutableMap.copyOf(methods.get(c)));
		}
	}
	
	public static AttributeResolver create(Object instance)
	{
		try{
			return build(instance);
		}catch(XacmlSyntaxException e){
			throw new IllegalArgumentException(e);
		}
	}
	
	@Override
	protected BagOfAttributeValues doResolve(
			PolicyInformationPointContext context, 
			AttributeCategory category,
			String attributeId,
			AttributeValueType dataType,
			String issuer) 
				throws Exception 
	{
		Map<String, Method> byId = resolvers.get(category);
		Preconditions.checkState(byId != null);
		Method m = byId.get(attributeId);
		Preconditions.checkState(m != null);
		if(log.isDebugEnabled()){
			log.debug("Invoking resolver=\"{}\" method=\"{}\"", 
					getDescriptor().getName(), m.getName());
		}
		try{
			return (BagOfAttributeValues)m.invoke(instance, new Object[]{context});
		}catch(Exception e){
			if(log.isDebugEnabled()){
				log.debug(e.getMessage(), e);
			}
			throw e;
		}
		
	}	
	
	private static AttributeResolver build(Object instance) throws XacmlSyntaxException
	{
		Preconditions.checkArgument(instance != null);
		XacmlAttributeResolverDescriptor descriptor = instance.getClass().getAnnotation(XacmlAttributeResolverDescriptor.class);
		Preconditions.checkState(descriptor != null);
		XacmlAttributeIssuer issuer = instance.getClass().getAnnotation(XacmlAttributeIssuer.class);
		List<Method> resolvers = Reflections.getAnnotatedMethods(instance.getClass(), XacmlAttributeDescriptor.class);
		AttributeResolverDescriptorBuilder builder = AttributeResolverDescriptorBuilder.create(descriptor.name(), 
				(issuer == null)?null:issuer.value());
		Map<AttributeCategory, Map<String, Method>> methods = new HashMap<AttributeCategory, Map<String,Method>>();
		for(Method r : resolvers)
		{
			XacmlAttributeDescriptor d = r.getAnnotation(XacmlAttributeDescriptor.class);
			XacmlAttributeCategory c = r.getAnnotation(XacmlAttributeCategory.class);
			Preconditions.checkState(c != null, 
					"Class=\"%s\" method=\"%s\" does not have attribute category specified", 
					instance.getClass().getName(), r.getName());
			validateResolverMethod(r);
			for(String cat : c.value())
			{
				AttributeCategory category = AttributeCategories.parse(cat);
				builder.attribute(category, d.id(), XacmlDataTypesRegistry.getType(d.typeId()));
				Map<String, Method> byId = methods.get(category);
				if(byId == null){
					byId = new HashMap<String, Method>();
					methods.put(category, byId);
				}
				r.setAccessible(true);
				byId.put(d.id(), r);
			}
		}
		return new AnnotatedAttributeResolver(builder.build(), methods, instance);
	} 
	
	private static void validateResolverMethod(Method m)
	{
		if(log.isDebugEnabled()){
			log.debug("Validating resolver method=\"{}\" with=\"{}\" parameters" , m.getName(),  
					(m.getParameterTypes() == null)?0:m.getParameterTypes().length);
		}
		Class<?>[] p = m.getParameterTypes();
		Preconditions.checkArgument(!(p == null || p.length == 0), 
				"Attribute resolver method should have at least on parameter");
		Preconditions.checkArgument(PolicyInformationPointContext.class.isAssignableFrom(p[0]),
				"First attribute resolver method parameter must be of type=\"%s", 
				PolicyInformationPointContext.class.getName());
		
		Preconditions.checkArgument(m.getReturnType().isAssignableFrom(BagOfAttributeValues.class), 
				"Attribute resolver method should return value of type=\"%s\"", BagOfAttributeValues.class.getName());
		if(p.length == 1){
			return;
		}
		Annotation[][] params = m.getParameterAnnotations();
		for(int i = 1; i < p.length; i++)
		{
			if(log.isDebugEnabled()){
				log.debug("Validating resolver method=\"{}\" parameter of type=\"{}\"" , m.getName(),  
						p[i].getName());
			}
			
			if(params[i] == null || 
					params[i].length == 0 || 
					!(params[i][0] instanceof XacmlAttributeKey)){
				throw new IllegalArgumentException(String.format(
						"Additional attribute resolver method=\"%s\" parameter at index=\"%d\" must be " +
						"annotiated via=\"%s\" annotation", 
						m.getName(), i, XacmlAttributeKey.class.getName()));
			}
			XacmlAttributeKey key = (XacmlAttributeKey)params[i][0];
			if(key.isBag() && 
					!p[i].isAssignableFrom(BagOfAttributeValues.class)){
				throw new IllegalArgumentException(String.format(
						"Attribuite resolver method=\"%s\" annotation=\"%s\" " +
						"defines XACML bag but parameter at index=\"%d\" is of type=\"%s\"", 
						m.getName(), XacmlAttributeKey.class.getName(),i, p[i].getName()));
			}
			if(!(!key.isBag() && 
					AttributeValue.class.isAssignableFrom(p[i]))){
				throw new IllegalArgumentException(String.format(
						"Attribuite resolver method=\"%s\" annotation=\"%s\" " +
						"defines XACML scalar type but  parameter at index=\"%d\" is of type=\"%s\"", 
						m.getName(), XacmlAttributeKey.class.getName(), i, p[i].getName()));
			}
		}
	}
	
	class AttributeKeyInfo
	{
		private String attributeId;
		private AttributeValueType dataType;
		private boolean singleValue;
		
		public AttributeKeyInfo(String attributeId, 
				AttributeValueType dataType, boolean singleValue){
			this.attributeId = attributeId;
			this.dataType = dataType;
			this.singleValue = singleValue;
		}
		
		public ValueExpression getKey(AttributeCategory category, 
				RequestContextCallback callback){
			BagOfAttributeValues bag = callback.getAttributeValues(category, attributeId, dataType);
			if(bag == null || bag.isEmpty()){
				return bag;
			}
			return singleValue?bag.value():bag;
		}
	}
}
