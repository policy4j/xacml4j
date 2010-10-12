package com.artagon.xacml.v3.spi.pip.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.RequestContextCallback;
import com.artagon.xacml.v3.ValueExpression;
import com.artagon.xacml.v3.XacmlObject;
import com.artagon.xacml.v3.XacmlSyntaxException;
import com.artagon.xacml.v3.marshall.XacmlDataTypesRegistry;
import com.artagon.xacml.v3.sdk.XacmlAttributeKey;
import com.artagon.xacml.v3.spi.pip.PolicyInformationPointContext;
import com.google.common.base.Preconditions;

public class RequestContextKeyInfo extends XacmlObject
{
	private final static Logger log = LoggerFactory.getLogger(RequestContextKeyInfo.class);
	
	private String attributeId;
	private AttributeValueType dataType;
	private boolean singleValue;
	
	public RequestContextKeyInfo(String attributeId, 
			AttributeValueType dataType, boolean singleValue){
		this.attributeId = attributeId;
		this.dataType = dataType;
		this.singleValue = singleValue;
	}
	
	public ValueExpression getKey(PolicyInformationPointContext context)
	{
		RequestContextCallback callback = context.getRequestContextCallback();
		BagOfAttributeValues bag = callback.getAttributeValues(context.getCategory(), attributeId, dataType);
		if(bag == null || 
				bag.isEmpty()){
			return dataType.emptyBag();
		}
		return singleValue?bag.value():bag;
	}
	
	public static List<RequestContextKeyInfo> getRequestKeyInfo(Method m) throws XacmlSyntaxException
	{
		List<RequestContextKeyInfo> keyInfo = new LinkedList<RequestContextKeyInfo>();
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
			if(log.isDebugEnabled()){
				log.debug("No request keys " +
						"found in method=\"{}\" signature", m.getName());
			}
			return keyInfo;
		}
		Annotation[][] params = m.getParameterAnnotations();
		for(int i = 1; i < params.length; i++)
		{
			if(log.isDebugEnabled()){
				log.debug("Validating resolver method=\"{}\" " +
						"parameter of type=\"{}\"" , m.getName(),  
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
						"Attribute resolver method=\"%s\" annotation=\"%s\" " +
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
			if(log.isDebugEnabled()){
				
			}
			if(log.isDebugEnabled()){
				log.debug("Adding request key id=\"{}\", " +
						"category=\"{}\" type=\"{}\"", 
						new Object[]{ key.id(), key.category(), key.type()});
			}
			keyInfo.add(new RequestContextKeyInfo(key.id(), 
					XacmlDataTypesRegistry.getType(key.type()), !key.isBag()));
		}
		return keyInfo;
	}
}

