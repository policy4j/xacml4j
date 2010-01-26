package com.artagon.xacml.v3.policy.type;

import com.artagon.xacml.util.Preconditions;

public final class RFC822Name 
{
	private String localPart;
	private String domainPart;
	private String fqName;
	
	public RFC822Name(String localPart, 
			String domainPart){
		Preconditions.checkNotNull(localPart);
		Preconditions.checkNotNull(domainPart);
		this.domainPart = domainPart;
		this.localPart = localPart;
		this.fqName = new StringBuilder()
		.append(localPart)
		.append('@')
		.append(domainPart)
		.toString();
	}
	
	public String getLocalPart(){
		return localPart;
	}
	
	public String getDomainPart(){
		return domainPart;
	}
	
	@Override
	public String toString(){
		return fqName;
	}
	
	@Override
	public int hashCode(){
		return fqName.hashCode();
	}
	
	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(!(o instanceof RFC822Name)){
			return false;
		}
		RFC822Name n = (RFC822Name)o;
		return n.getLocalPart().equals(getLocalPart()) &&
		n.getDomainPart().equalsIgnoreCase(getDomainPart());
	}
}
