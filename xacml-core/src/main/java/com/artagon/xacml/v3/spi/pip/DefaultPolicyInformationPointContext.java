package com.artagon.xacml.v3.spi.pip;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import com.artagon.xacml.v3.AttributeReferenceKey;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationException;
import com.google.common.base.Preconditions;

public final class DefaultPolicyInformationPointContext implements
		PolicyInformationPointContext 
{
	private EvaluationContext context;
	private List<BagOfAttributeValues> keys;
	private ResolverDescriptor desciptor;

	public DefaultPolicyInformationPointContext(
			EvaluationContext context,
			ResolverDescriptor descriptor) {
		Preconditions.checkNotNull(context);
		Preconditions.checkNotNull(descriptor);
		this.context = context;
		this.desciptor = descriptor;
	}

	@Override
	public Calendar getCurrentDateTime() {
		return context.getCurrentDateTime();
	}

	@Override
	public List<BagOfAttributeValues> getKeys() throws EvaluationException
	{
		if(keys != null){
			return keys;
		}
		List<AttributeReferenceKey> keyRefs = desciptor.getKeyRefs();
		this.keys = new ArrayList<BagOfAttributeValues>(keyRefs.size());
		for(AttributeReferenceKey ref : keyRefs){
			keys.add(ref.resolve(context));
		}
		this.keys = Collections.unmodifiableList(this.keys);
		return keys;
	}
	
}
