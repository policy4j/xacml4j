package com.artagon.xacml.v30.spi.pip;

import java.util.Calendar;
import java.util.List;

import com.artagon.xacml.v30.AttributeReferenceKey;
import com.artagon.xacml.v30.BagOfAttributeExp;
import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.EvaluationException;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Ticker;
import com.google.common.collect.ImmutableList;

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
	public Ticker getTicker(){
		return context.getTicker();
	}
	
	@Override
	public ResolverDescriptor getDescriptor(){
		return desciptor;
	}
	
	@Override
	public List<BagOfAttributeExp> getKeys(){
		return keys;
	}
	
	private static List<BagOfAttributeExp> evaluateKeys(EvaluationContext context, 
			List<AttributeReferenceKey> keyRefs) throws EvaluationException
	{
		if(keyRefs == null){
			return ImmutableList.of();
		}
		ImmutableList.Builder<BagOfAttributeExp> b = ImmutableList.builder();
		for(AttributeReferenceKey ref : keyRefs){
			b.add(ref.resolve(context));
		}
		return b.build();
	}
	
	@Override
	public String toString(){
		return Objects.toStringHelper(this)
				.add("context", context)
				.add("descriptor", desciptor)
				.add("keys", keys)
				.toString();
	}
	
}
