package com.artagon.xacml.v30.policy.type;

import com.artagon.xacml.util.Preconditions;

/**
 * An implementation of XACML {@link BooleanType}
 * 
 * @author Giedrius Trumpickas
 */
final class BooleanTypeImpl extends 
	BaseAttributeType<BooleanType.BooleanValue> implements BooleanType
{
	private BooleanValue FALSE;
	private BooleanValue TRUE;
	
	/**
	 * Constructs XACML type with a given
	 * type identifier
	 * 
	 * @param typeId a XACML type identifier
	 */
	BooleanTypeImpl(String typeId){
		super(typeId, Boolean.class);
		this.FALSE = new BooleanValue(this, Boolean.FALSE);
		this.TRUE = new BooleanValue(this, Boolean.TRUE);
	}
	
	@Override
	public BooleanValue create(Object any){
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertableFrom(any),String.format(
				"Value=\"%s\" of class=\"%s\" can't ne converted to XACML \"boolean\" type", 
				any, any.getClass()));
		if(String.class.isInstance(any)){
			return fromXacmlString((String)any);
		}
		return ((Boolean)any)?TRUE:FALSE;
	}

	@Override
	public BooleanValue fromXacmlString(String v) {
		Preconditions.checkNotNull(v);
		return Boolean.parseBoolean(v)?TRUE:FALSE;
	}
}