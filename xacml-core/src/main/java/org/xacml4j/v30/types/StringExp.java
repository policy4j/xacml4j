package org.xacml4j.v30.types;

public final class StringExp extends BaseAttributeExp<String>
{
	private static final long serialVersionUID = 657672949137533611L;

	StringExp(StringType type, String value) {
		super(type, value);
	}

	public boolean equalsIgnoreCase(StringExp v){
		return getValue().equalsIgnoreCase(v.getValue());
	}
}

