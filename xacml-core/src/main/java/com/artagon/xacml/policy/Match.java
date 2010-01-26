package com.artagon.xacml.policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.policy.type.BooleanType.BooleanValue;
import com.artagon.xacml.util.Preconditions;

/**
 *  
 * @author Giedrius Trumpickas
 */
public final class Match implements Matchable, PolicyElement
{	
	private final static Logger log = LoggerFactory.getLogger(Match.class);
	
	private AttributeValue value;
	private AttributeReference attributeRef;
	private FunctionSpec predicate;
	
	/**
	 * Constructs match with a given literal value 
	 * and attribute reference.
	 * @param value a literal attribute value
	 * @param attrRef an attribute reference
	 * @param function a match function
	 */
	protected Match(FunctionSpec spec, 
			AttributeValue value, AttributeReference attributeReference)
	{
		Preconditions.checkNotNull(spec);
		Preconditions.checkNotNull(value);
		Preconditions.checkNotNull(attributeReference);
		Preconditions.checkArgument(spec.getNumberOfParams() == 2);
		Preconditions.checkArgument(spec.getParamSpecs().get(0).
				isValidParamType(value.getEvaluatesTo()));
		Preconditions.checkArgument(spec.getParamSpecs().get(1).
				isValidParamType((attributeReference.getDataType())));
		this.value = value;
		this.predicate = spec;
		this.attributeRef = attributeReference;	
	}
	
	/**
	 * Gets match function XACML identifier.
	 * 
	 * @return match function XACML identifier
	 */
	public String getMatchId(){
		return predicate.getXacmlId();
	}
	
	/**
	 * Gets match attribute value.
	 * 
	 * @return {@link Attribute<?>} instance
	 */
	public AttributeValue getAttributeValue(){
		return value;
	}
	
	@Override
	public MatchResult match(EvaluationContext context)
	{
		try
		{
			BagOfAttributes<?> attributes = (BagOfAttributes<?>)attributeRef.evaluate(context);
			log.debug("Evaluated attribute reference=\"{}\" to " +
					"bag=\"{}\"", attributeRef, attributes);
			for(AttributeValue attr : attributes.values()){
				BooleanValue match = (BooleanValue)predicate.invoke(context, attr, attributeRef);
				if(match.getValue()){
					log.debug("Attribute value=\"{}\" " +
							"matches attribute value=\"{}\"", value, attr);
					return MatchResult.MATCH;
				}
			}
			return MatchResult.NOMATCH;
		}catch(EvaluationException e){
			log.debug("Failed to evaluate match predicate=\"{}\", " +
					"attribute value=\"{}\"", predicate.getXacmlId(), value);
			return MatchResult.INDETERMINATE;
		}
	}

	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		value.accept(v);
		attributeRef.accept(v);
		v.visitLeave(this);
	}
}
