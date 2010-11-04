package com.artagon.xacml.v3.spi.pip;

import java.util.Calendar;

import com.artagon.xacml.v3.AttributeReferenceKey;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.EvaluationContext;
import com.google.common.base.Preconditions;

public final class DefaultPolicyInformationPointContext implements
		PolicyInformationPointContext 
{
	private EvaluationContext context;
	private BagOfAttributeValues[] keys;
	private ResolverDescriptor desciptor;

	public DefaultPolicyInformationPointContext(EvaluationContext context,
			ResolverDescriptor descriptor, BagOfAttributeValues[] keys) {
		Preconditions.checkNotNull(context);
		Preconditions.checkNotNull(descriptor);
		Preconditions.checkNotNull(keys);
		this.context = context;
		this.keys = keys;
		this.desciptor = descriptor;
	}

	@Override
	public Calendar getCurrentDateTime() {
		return context.getCurrentDateTime();
	}

	@Override
	public BagOfAttributeValues getKeyValues(int index) {
		AttributeReferenceKey ref = desciptor.getKeyAt(index);
		return (keys[index] == null) ? ref.getDataType().emptyBag()
				: keys[index];
	}

	@Override
	public <V extends AttributeValue> V getKeyValue(int index) {
		BagOfAttributeValues v = getKeyValues(index);
		if (v.isEmpty()) {
			return null;
		}
		return v.value();
	}
}
