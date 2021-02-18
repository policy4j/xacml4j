package org.xacml4j.v30.spi.pip;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.util.Calendar;
import java.util.List;

import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Ticker;
import com.google.common.collect.ImmutableList;

final class DefaultResolverContext implements
		ResolverContext
{
	private EvaluationContext context;
	private List<BagOfAttributeExp> keys;
	private ResolverDescriptor descriptor;

	public DefaultResolverContext(
			EvaluationContext context,
			ResolverDescriptor descriptor) throws EvaluationException {
		Preconditions.checkNotNull(context);
		Preconditions.checkNotNull(descriptor);
		this.context = context;
		this.descriptor = descriptor;
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
		return descriptor;
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
			BagOfAttributeExp v = ref.resolve(context);
			if (v != null) {
				b.add(v);
			} else {
				b.add(ref.getDataType().emptyBag());
			}
		}
		return b.build();
	}

	@Override
	public String toString(){
		return Objects.toStringHelper(this)
				.add("context", context)
				.add("descriptor", descriptor)
				.add("keys", keys)
				.toString();
	}

}
