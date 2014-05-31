package org.xacml4j.v30.pdp;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.MatchResult;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class Match implements PolicyElement, Matchable
{
	private final static Logger log = LoggerFactory.getLogger(Match.class);

	private final AttributeExp value;
	private final AttributeReference attributeRef;
	private final FunctionSpec predicate;

	/**
	 * Constructs match.
	 *
	 * @param b a match builder
	 */
	private Match(Builder b)
	{
		Preconditions.checkNotNull(b.attr);
		Preconditions.checkNotNull(b.attrRef);
		Preconditions.checkNotNull(b.predicate);
		Preconditions.checkArgument(b.predicate.getNumberOfParams() == 2,
				"Expecting function with 2 arguments");
		Preconditions.checkArgument(b.predicate.getParamSpecAt(0).
				isValidParamType(b.attr.getEvaluatesTo()),
				"Given function argument at index=\"0\" type is not compatible with a given attribute value type");
		Preconditions.checkArgument(b.predicate.getParamSpecAt(1).
				isValidParamType((b.attrRef.getDataType())),
				"Given function argument at index=\"1\" type is not compatible with a given attribute reference type");
		this.value = b.attr;
		this.predicate = b.predicate;
		this.attributeRef = b.attrRef;
	}

	public static Builder builder(){
		return new Builder();
	}
	/**
	 * Gets match function XACML identifier.
	 *
	 * @return match function XACML identifier
	 */
	public String getMatchId(){
		return predicate.getId();
	}

	/**
	 * Gets match attribute value.
	 *
	 * @return {@link AttributeExp} instance
	 */
	public AttributeExp getAttributeValue(){
		return value;
	}

	/**
	 * Gets attribute reference
	 *
	 * @return {@link AttributeReference}
	 */
	public AttributeReference getReference(){
		return attributeRef;
	}

	@Override
	public MatchResult match(EvaluationContext context)
	{
		try
		{
			BagOfAttributeExp attributes = attributeRef.evaluate(context);
			if(log.isDebugEnabled()){
				log.debug("Evaluated attribute reference=\"{}\" to " +
						"bag=\"{}\"", attributeRef, attributes);
			}
			for(AttributeExp v : attributes.values()){
				AttributeExp match = predicate.invoke(context, value, v);
				if((Boolean)match.getValue()){
					if(log.isDebugEnabled()){
						log.debug("Attribute value=\"{}\" " +
								"matches attribute value=\"{}\"", value, v);
					}
					return MatchResult.MATCH;
				}
			}
			return MatchResult.NOMATCH;
		}catch(EvaluationException e){
			if(log.isDebugEnabled()){
				log.debug("Match evaluation " +
						"failed with an exception", e);
			}
			context.setEvaluationStatus(e.getStatus());
			return MatchResult.INDETERMINATE;
		}
	}

	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(!(o instanceof Match)){
			return false;
		}
		Match m = (Match)o;
		return predicate.equals(m.predicate) &&
				value.equals(m.value) &&
				attributeRef.equals(m.attributeRef);
	}

	@Override
	public String toString(){
		return Objects.toStringHelper(this)
				.add("MatchId", predicate.getId())
				.add("Value", value)
				.add("Reference", attributeRef)
				.toString();
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(
				predicate,
				value,
				attributeRef);
	}

	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		v.visitLeave(this);
	}

	public static class Builder
	{
		private AttributeExp attr;
		private FunctionSpec predicate;
		private AttributeReference attrRef;

		public Builder attribute(AttributeExp v){
			Preconditions.checkNotNull(v);
			this.attr = v;
			return this;
		}

		public Builder predicate(FunctionSpec f){
			Preconditions.checkNotNull(f);
			this.predicate = f;
			return this;
		}

		public Builder attrRef(AttributeReference ref){
			Preconditions.checkNotNull(ref);
			this.attrRef = ref;
			return this;
		}

		public Match build(){
			return new Match(this);
		}
	}
}
