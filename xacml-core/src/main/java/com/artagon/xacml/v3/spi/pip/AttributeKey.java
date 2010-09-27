package com.artagon.xacml.v3.spi.pip;

import java.util.Arrays;

import com.artagon.xacml.v3.AttributeCategory;
import com.artagon.xacml.v3.ValueExpression;
import com.google.common.base.Preconditions;

public final class AttributeKey 
{
	private AttributeCategory category;
	private ValueExpression[] keys;
	private int hashCode;
	
	public AttributeKey(AttributeCategory category, 
			ValueExpression ...keys){
		Preconditions.checkArgument(category != null);
		Preconditions.checkArgument(keys != null);
		this.keys = keys;
		this.hashCode = 31 * category.hashCode() + Arrays.hashCode(keys);
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj  == this){
			return true;
		}
		if(!(obj instanceof AttributeKey)){
			return false;
		}
		AttributeKey k = (AttributeKey)obj;
		return category.equals(k.category) && 
		Arrays.equals(keys, k.keys);
	}
}
