package org.xacml4j.v30.pdp;

import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.StatusCode;

import com.google.common.base.Preconditions;


public class AttributeReferenceEvaluationException extends EvaluationException
{
	private static final long serialVersionUID = 4604041385261391333L;

	private AttributeReferenceKey ref;

	public AttributeReferenceEvaluationException(
			AttributeReferenceKey ref,
			String message, Object ...params){
		super(StatusCode.createMissingAttributeError(),
				message, params);
		this.ref = ref;
	}

	public AttributeReferenceEvaluationException(
			StatusCode statusCode,
			EvaluationContext context,
			AttributeReferenceKey ref,
			String message, Object ...params){
		super(statusCode, message, params);
		Preconditions.checkNotNull(ref);
		this.ref = ref;
	}

	public AttributeReferenceEvaluationException(
			EvaluationContext context,
			AttributeReferenceKey ref,
			StatusCode code,
			Throwable cause){
		super(code, 
				cause);
		this.ref = ref;
	}

	public AttributeReferenceKey getReference(){
		return ref;
	}
}
