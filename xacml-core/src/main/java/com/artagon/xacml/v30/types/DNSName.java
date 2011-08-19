package com.artagon.xacml.v30.types;

import com.artagon.xacml.v30.AttributeValue;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.net.InternetDomainName;

public final class DNSName
{
	private static final long serialVersionUID = -1729624624549215684L;
	
	private InternetDomainName name;
	private PortRange portRange;
	
	public DNSName(String name, PortRange range)
	{
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
		if(!(o instanceof DNSName)){
			return false;
		}
		DNSName n = (DNSName)o;
		return name.equals(n.name) && portRange.equals(n.portRange);
	}
	
		
	@Override
	public String toString(){
		return toXacmlString();
	}

	public String toXacmlString() {
		StringBuilder b = new StringBuilder(name.name());
		if(!portRange.isUnbound()){
			b.append(":").append(portRange.toString());
		}
		return b.toString();
	}		
}
