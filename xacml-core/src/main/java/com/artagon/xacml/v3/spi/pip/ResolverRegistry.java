package com.artagon.xacml.v3.spi.pip;

import com.artagon.xacml.v3.AttributeCategory;
import com.artagon.xacml.v3.AttributeDesignatorKey;
import com.artagon.xacml.v3.EvaluationContext;

public interface ResolverRegistry 
{
	AttributeResolver getAttributeResolver(
			EvaluationContext context, AttributeDesignatorKey key);
	
	ContentResolver getContentResolver(
			EvaluationContext contetx, AttributeCategory category);
	
	void addResolver(AttributeResolver r);
	
	void addResolver(ContentResolver r);
	
	void addResolver(String policyId, AttributeResolver r);
	void addResolver(String policyId, ContentResolver r);
	
	AttributeResolver getAttributeResolver(String id);
	ContentResolver getContentResolver(String id);
}
