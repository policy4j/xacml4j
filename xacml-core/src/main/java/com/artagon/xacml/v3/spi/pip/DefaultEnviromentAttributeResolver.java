package com.artagon.xacml.v3.spi.pip;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeDesignator;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.RequestAttributesCallback;
import com.artagon.xacml.v3.types.XacmlDataTypes;

class DefaultEnviromentAttributeResolver extends BaseAttributeResolver
{	
	private final static String CURRENT_TIME = "urn:oasis:names:tc:xacml:1.0:environment:current-time";
	private final static String CURRENT_DATE = "urn:oasis:names:tc:xacml:1.0:environment:current-date";
	private final static String CURRENT_DATETIME = "urn:oasis:names:tc:xacml:1.0:environment:current-dateTime";
	
	public DefaultEnviromentAttributeResolver(){
		super(AttributeResolverDescriptorBuilder.create(AttributeCategoryId.ENVIRONMENT).
				attribute(CURRENT_DATE).
				attribute(CURRENT_TIME).
				attribute(CURRENT_DATETIME).build());
	}
	
	@Override
	public BagOfAttributeValues<? extends AttributeValue> resolve(
			PolicyInformationPointContext context,
			AttributeDesignator ref, RequestAttributesCallback callback) 
	{
		if(ref.getAttributeId().equals(CURRENT_DATETIME)){
			 return XacmlDataTypes.DATETIME.bag(context.getCurrentDateTime());
		}
		if(ref.getAttributeId().equals(CURRENT_DATE)){
			return XacmlDataTypes.DATE.bag(context.getCurrentDate());
		}
		if(ref.getAttributeId().equals(CURRENT_TIME)){
			return XacmlDataTypes.TIME.bag(context.getCurrentTime());
		}
		return ref.getDataType().bagOf().createEmpty();
	}
}
