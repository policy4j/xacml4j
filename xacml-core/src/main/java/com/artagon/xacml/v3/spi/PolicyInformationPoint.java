package com.artagon.xacml.v3.spi;

import org.w3c.dom.Node;

import com.artagon.xacml.v3.AttributeCategory;
import com.artagon.xacml.v3.AttributeDesignator;
import com.artagon.xacml.v3.AttributeReferenceEvaluationException;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.RequestContextCallback;

/**
 * A XACML Policy Information Point interface
 * 
 * @author Giedrius Trumpickas
 */
public interface PolicyInformationPoint 
{
	/**
	 * Resolves a given {@link AttributeDesignator} to
	 * a instance of {@link BagOfAttributeValues}
	 * 
	 * @param context an evaluation context
	 * @param ref an attribute designator
	 * @param callback an attributes callback
	 * @return 
	 * @throws AttributeReferenceEvaluationException
	 */
	BagOfAttributeValues resolve(
			EvaluationContext context, 
			AttributeDesignator ref, 
			RequestContextCallback callback) 
				throws EvaluationException;
	
	/**
	 * Resolves content for a given attribute category
	 * from an external source
	 * 
	 * @param context an evaluation context
	 * @param category an attribute category
	 * @param callback a request callback
	 * @return {@link Node} or <code>null</code>
	 */
	Node resolve(
			EvaluationContext context, 
			AttributeCategory categoryId, 
			RequestContextCallback callback) 
				throws EvaluationException;
	
}
