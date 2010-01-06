package com.artagon.xacml.policy.type;

import java.util.regex.Pattern;

import com.artagon.xacml.DataTypes;
import com.artagon.xacml.policy.BaseAttributeDataType;
import com.artagon.xacml.util.Preconditions;

final class RFC822NameTypeImpl extends BaseAttributeDataType<RFC822NameType.RFC822NameValue> implements RFC822NameType
{
	private final String RFC2822_REGEXP = 
		"[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)" +
		"*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?";
	
    
	private Pattern rfc2822;
	
	public RFC822NameTypeImpl(){
    	super(DataTypes.RFC822NAME, RFC822Name.class);    	
    	this.rfc2822 = Pattern.compile(RFC2822_REGEXP);
    }
    
	@Override
	public RFC822NameValue fromXacmlString(String v)
	{
		Preconditions.checkArgument(rfc2822.matcher(v).find());
        String [] parts = v.split("@");
        if (parts.length != 2) {
            throw new IllegalArgumentException(
            		String.format("Given value=\"%s\" is invalid RFC822 name", v));
        }
        return new RFC822NameValue(this, new RFC822Name(parts[0], parts[1]));
	}
	
	@Override
	public RFC822NameValue create(Object any)
	{
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertableFrom(any),String.format(
				"Value=\"%s\" of class=\"%s\" can't ne converted to XACML \"rfc822Name\" type", 
				any, any.getClass()));
		if(String.class.isInstance(any)){
			return fromXacmlString((String)any);
		}
		return new RFC822NameValue(this, (RFC822Name)any);
	}
}
