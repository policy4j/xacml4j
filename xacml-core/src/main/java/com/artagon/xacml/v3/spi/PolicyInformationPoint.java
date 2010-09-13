package com.artagon.xacml.v3.spi;

import org.w3c.dom.Node;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeDesignator;
import com.artagon.xacml.v3.AttributeReferenceEvaluationException;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.RequestContextAttributesCallback;
import com.artagon.xacml.v3.spi.pip.AttributeResolver;

public interface PolicyInformationPoint 
{
	BagOfAttributeValues<AttributeValue> resolve(
			EvaluationContext context, 
			AttributeDesignator ref, 
			RequestContextAttributesCallback callback) 
				throws AttributeReferenceEvaluationException;
	
	Node resolve(
			EvaluationContext context, 
			AttributeCategoryId categoryId, 
			RequestContextAttributesCallback callback);
	
	/**
	 * Adds attribute resolver
	 * 
	 * @param resolver an attribute resolver
	 */
	void addResolver(AttributeResolver resolver);
	
	/**
	 * Adds attribute resolver with the scope
	 * of a given policy or policy set and
	 * down bellow evaluation tree
	 * 
	 * @param policyId a policy or policy set identifier
	 * @param resolver an attribute resolver
	 */
	void addResolver(String policyId, AttributeResolver resober);
}
