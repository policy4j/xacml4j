package org.xacml4j.v30.pdp;

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

	private AttributeExp value;
	private AttributeReference attributeRef;
	private FunctionSpec predicate;

	/**
	 * Constructs match.
	 *
	 * @param b a match builder
	 */
	private Match(Builder b)
	{
		Preconditions.checkNotNull(b.attr);
		Preconditions.checkNotNull(b.attrRef);
		Preconditions.checkNotNull(b.predFunc);
		Preconditions.checkArgument(b.predFunc.getNumberOfParams() == 2,
				"Excpeting function with 2 arguments");
		Preconditions.checkArgument(b.predFunc.getParamSpecAt(0).
				isValidParamType(b.attr.getEvaluatesTo()),
				"Given function argument at index=\"0\" type is not compatible with a given attribute value type");
		Preconditions.checkArgument(b.predFunc.getParamSpecAt(1).
				isValidParamType((b.attrRef.getDataType())),
				"Given function argument at index=\"1\" type is not compatible with a given attribute reference type");
		this.value = b.attr;
		this.predicate = b.predFunc;
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
	 * @return {@link AttributeExp<?>} instance
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
			context.setEvaluationStatus(e.getStatusCode());
			return MatchResult.INDETERMINATE;
		}
	}

	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(o == null){
			return false;
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
		private FunctionSpec predFunc;
		private AttributeReference attrRef;

		public Builder attribute(AttributeExp v){
			Preconditions.checkNotNull(v);
			this.attr = v;
			return this;
		}

		public Builder predicate(FunctionSpec f){
			Preconditions.checkNotNull(f);
			this.predFunc = f;
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
