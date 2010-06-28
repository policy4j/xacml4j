package com.artagon.xacml.v3.spi.resolver;

import static com.artagon.xacml.v3.types.XacmlDataTypes.DATE;
import static com.artagon.xacml.v3.types.XacmlDataTypes.DATETIME;
import static com.artagon.xacml.v3.types.XacmlDataTypes.TIME;

import java.util.Calendar;
import java.util.TimeZone;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeDesignator;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.RequestAttributesCallback;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.spi.pip.AttributeResolverDescriptorBuilder;

public class DefaultEnviromentAttributeLocator extends BaseAttributeResolver
{	
	private final static String CURRENT_TIME = "urn:oasis:names:tc:xacml:1.0:environment:current-time";
	private final static String CURRENT_DATE = "urn:oasis:names:tc:xacml:1.0:environment:current-date";
	private final static String CURRENT_DATETIME = "urn:oasis:names:tc:xacml:1.0:environment:current-dateTime";
	
	public DefaultEnviromentAttributeLocator(){
		super(AttributeResolverDescriptorBuilder.create().
				withCategory(AttributeCategoryId.ENVIRONMENT).
				withAttribute(CURRENT_DATE).
				withAttribute(CURRENT_TIME).
				withAttribute(CURRENT_DATETIME).build());
	}
	
	@Override
	protected BagOfAttributeValues<? extends AttributeValue> doResolve(
			AttributeDesignator ref, RequestAttributesCallback callback) 
	{
		Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		if(ref.getAttributeId().equals(CURRENT_DATETIME)){
			 return DATETIME.bag(DATETIME.getType().create(now));
		}
		if(ref.getAttributeId().equals(CURRENT_DATE)){
			 return DATE.bag(DATE.getType().create(now));
		}
		if(ref.getAttributeId().equals(CURRENT_TIME)){
			 return TIME.bag(TIME.getType().create(now));
		}
		return ref.getDataType().bagOf().createEmpty();
	}
}
