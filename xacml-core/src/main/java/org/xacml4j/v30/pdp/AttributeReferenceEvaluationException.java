package org.xacml4j.v30.pdp;

import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Status;

import com.google.common.base.Preconditions;


public class AttributeReferenceEvaluationException extends EvaluationException
{
	private static final long serialVersionUID = 4604041385261391333L;

	private AttributeReferenceKey ref;
	
	public AttributeReferenceEvaluationException(AttributeDesignatorKey ref, 
			String format, Object ... args){
		this(Status.missingAttribute(ref).build(), ref, String.format(format, args));
	}
	
	public AttributeReferenceEvaluationException(AttributeDesignatorKey ref){
		this(ref, ref.getAttributeId());
	}
	
	public AttributeReferenceEvaluationException(AttributeSelectorKey ref, 
			String format, Object ... args){
		this(Status.missingAttribute(ref).build(), ref, String.format(format, args));
	}
	
	public AttributeReferenceEvaluationException(
			Status status, 
			AttributeReferenceKey key, 
			String format, Object ... args){
		super(status, String.format(format, args));
		Preconditions.checkNotNull(key);
		this.ref = key;
	}

	public AttributeReferenceEvaluationException(
			AttributeSelectorKey ref){
		this(ref, ref.getPath());
	}

	public AttributeReferenceKey getReference(){
		return ref;
	}
}
