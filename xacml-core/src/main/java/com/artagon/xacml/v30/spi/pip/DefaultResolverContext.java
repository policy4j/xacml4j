package com.artagon.xacml.v30.spi.pip;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import com.artagon.xacml.v30.AttributeReferenceKey;
import com.artagon.xacml.v30.BagOfAttributesExp;
import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.EvaluationException;
import com.google.common.base.Preconditions;

public final class DefaultResolverContext implements
		ResolverContext 
{
	private EvaluationContext context;
	private List<BagOfAttributesExp> keys;
	private ResolverDescriptor desciptor;

	public DefaultResolverContext(
			EvaluationContext context,
			ResolverDescriptor descriptor) throws EvaluationException {
		Preconditions.checkNotNull(context);
		Preconditions.checkNotNull(descriptor);
		this.context = context;
		this.desciptor = descriptor;
		this.keys = evaluateKeys(context, descriptor.getKeyRefs());
	}

	@Override
	public Calendar getCurrentDateTime() {
		return context.getCurrentDateTime();
	}
	
	@Override
	public ResolverDescriptor getDescriptor(){
		return desciptor;
	}
	
	@Override
	public List<BagOfAttributesExp> getKeys()
	{
		return keys;
	}
	
	private static List<BagOfAttributesExp> evaluateKeys(EvaluationContext context, 
			List<AttributeReferenceKey> keyRefs) throws EvaluationException
	{
		if(keyRefs == null){
			return Collections.<BagOfAttributesExp>emptyList();
		}
		List<BagOfAttributesExp> keys = new ArrayList<BagOfAttributesExp>(keyRefs.size());
		for(AttributeReferenceKey ref : keyRefs){
			keys.add(ref.resolve(context));
		}
		return Collections.unmodifiableList(keys);
	}
	
}
