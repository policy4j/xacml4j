package com.artagon.xacml.v3.types;

import com.artagon.xacml.v3.AttributeValue;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.net.InternetDomainName;

public final class DNSNameValue extends BaseAttributeValue
{
	private static final long serialVersionUID = -1729624624549215684L;
	
	private InternetDomainName name;
	private PortRange portRange;
	
	public DNSNameValue(DNSNameType type, String name, PortRange range)
	{
		super(type);
		Preconditions.checkNotNull(name);
		Preconditions.checkNotNull(range);
		this.name = InternetDomainName.from(name);
		this.portRange = range;
	}
	
	public String getName(){
		return name.name();
	}
	
	public PortRange getPortRange(){
		return portRange;
	}
	
	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(!(o instanceof AttributeValue)){
			return false;
		}
		if(!(getType().equals(((AttributeValue)o).getType()))){
			return false;
		}
		if(!(o instanceof DNSNameValue)){
			return false;
		}
		DNSNameValue n = (DNSNameValue)o;
		return name.equals(n.name) && portRange.equals(n.portRange);
	}
	
		
	@Override
	public String toString(){
		return Objects.toStringHelper(this).
		add("Value", toXacmlString()).
		add("Type", getType()).toString();
	}

	@Override
	public String toXacmlString() {
		StringBuilder b = new StringBuilder(name.name());
		if(!portRange.isUnbound()){
			b.append(":").append(portRange.toString());
		}
		return b.toString();
	}		
	
	
}

