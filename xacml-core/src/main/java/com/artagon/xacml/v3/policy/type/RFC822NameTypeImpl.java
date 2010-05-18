package com.artagon.xacml.v3.policy.type;

import com.artagon.xacml.v3.policy.spi.type.BaseAttributeType;
import com.google.common.base.Preconditions;

final class RFC822NameTypeImpl extends BaseAttributeType<RFC822NameType.RFC822NameValue> implements RFC822NameType
{
	public RFC822NameTypeImpl(String typeId){
    	super(typeId);    	
    }
	
	@Override
	public boolean isConvertableFrom(Object any) {
		return String.class.isInstance(any) || RFC822Name.class.isInstance(any);
	}
	
	@Override
	public RFC822NameValue fromXacmlString(String v, Object ...params)
	{
		Preconditions.checkNotNull(v);
        return new RFC822NameValue(this, RFC822Name.parse(v));
	}
	
	@Override
	public RFC822NameValue create(Object any, Object ...params)
	{
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertableFrom(any),String.format(
				"Value=\"%s\" of class=\"%s\" can't ne converted to XACML \"rfc822Name\" type", 
				any, any.getClass()));
		if(any instanceof String){
			return fromXacmlString((String)any);
		}
		return new RFC822NameValue(this, (RFC822Name)any);
	}
}
