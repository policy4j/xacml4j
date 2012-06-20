package com.artagon.xacml.v30.spi.pip;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import com.artagon.xacml.v30.BagOfAttributeExp;
import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.EvaluationException;
import com.artagon.xacml.v30.pdp.AttributeReferenceKey;
import com.google.common.base.Preconditions;

final class DefaultResolverContext implements
		ResolverContext 
{
	private EvaluationContext context;
	private List<BagOfAttributeExp> keys;
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
	public List<BagOfAttributeExp> getKeys()
	{
		return keys;
	}
	
	private static List<BagOfAttributeExp> evaluateKeys(EvaluationContext context, 
			List<AttributeReferenceKey> keyRefs) throws EvaluationException
	{
		if(keyRefs == null){
			return Collections.<BagOfAttributeExp>emptyList();
		}
		List<BagOfAttributeExp> keys = new ArrayList<BagOfAttributeExp>(keyRefs.size());
		for(AttributeReferenceKey ref : keyRefs){
			keys.add(ref.resolve(context));
		}
		return Collections.unmodifiableList(keys);
	}
	
}
