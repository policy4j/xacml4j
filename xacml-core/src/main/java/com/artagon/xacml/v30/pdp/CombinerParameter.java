package com.artagon.xacml.v30.pdp;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * Conveys a single parameter for a policy- or rule-combining algorithm.
 * 
 * @author Giedrius Trumpickas
 */
public class CombinerParameter 
	implements PolicyElement
{
	private String name;
	private AttributeExp value;
	
	/**
	 * Constructs decision combining parameter
	 * 
	 * @param name a parameter name
	 * @param value a parameter value
	 */
	public CombinerParameter(String name, 
			AttributeExp value)
	{
		Preconditions.checkNotNull(name);
		Preconditions.checkNotNull(value);
		this.name = name;
		this.value = value;
	}
	
	public final String getName(){
		return name;
	}
	
	public final AttributeExp getValue(){
		return value;
	}
	
	@Override
	public int hashCode(){
		return Objects.hashCode(name, value);
	}
	
	@Override
	public String toString(){
		return Objects.toStringHelper(this)
		.add("name", name)
		.add("value", value)
		.toString();
	}
	
	@Override
	public boolean equals(Object  o){
		if(o == this){
			return true;
		}
		if(o == null){
			return false;
		}
		if(!(o instanceof CombinerParameter)){
			return false;
		}
		CombinerParameter c = (CombinerParameter)o;
		return name.equals(c.getName()) 
		&& value.equals(c.value);
	}

	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		v.visitLeave(this);
	}	
}
