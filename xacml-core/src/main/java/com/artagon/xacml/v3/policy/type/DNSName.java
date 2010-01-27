package com.artagon.xacml.v3.policy.type;

import java.io.Serializable;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.XacmlObject;

public final class DNSName extends XacmlObject implements Serializable
{
	private static final long serialVersionUID = 5614192765257814372L;
	private String name;
	private PortRange portRange;
	
	public DNSName(String name, PortRange range)
	{
		Preconditions.checkNotNull(name);
		Preconditions.checkNotNull(range);
		this.name = name;
		this.portRange = range;
	}
	
	public String getName(){
		return name;
	}
	
	public PortRange getPortRange(){
		return portRange;
	}
	
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
		StringBuilder b = new StringBuilder(name);
		if(!portRange.isUnbound()){
			b.append(":").append(portRange.toString());
		}
		return b.toString();
	}
}
