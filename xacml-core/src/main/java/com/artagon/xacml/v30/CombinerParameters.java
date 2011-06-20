package com.artagon.xacml.v30;

import com.google.common.base.Objects;


public class CombinerParameters 
	extends BaseDecisionCombinerParameters 
	implements PolicyElement
{
	public CombinerParameters(
			Iterable<CombinerParameter> parameters) {
		super(parameters);
	}
	
	@Override
	public int hashCode(){
		return parameters.hashCode();
	}
	
	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(o == null){
			return false;
		}
		if(!(o instanceof CombinerParameters)){
			return false;
		}
		CombinerParameters c = (CombinerParameters)o;
		return parameters.equals(c.parameters);
	}
	
	@Override
	public String toString(){
		return Objects.toStringHelper(this)
		.add("parameters", parameters)
		.toString();
	}
	
	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		for(CombinerParameter p : parameters.values()){
			p.accept(v);
		}
		v.visitLeave(this);
	}
}
