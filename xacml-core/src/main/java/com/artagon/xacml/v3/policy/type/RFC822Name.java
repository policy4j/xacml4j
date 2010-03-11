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
		this.domainPart = domainPart.toLowerCase();
		this.localPart = localPart;
		this.fqName = new StringBuilder()
		.append(this.localPart)
		.append('@')
		.append(this.domainPart)
		.toString();
	}
	
	public boolean matches(String pattern){
		int pos = pattern.indexOf('@');
		if(pos != -1){
			RFC822Name n = RFC822Name.parse(pattern);
			return n.equals(this);
		}
		if (pattern.charAt(0) == '.') {
             return domainPart.endsWith(pattern.toLowerCase());
		}
		return domainPart.equalsIgnoreCase(pattern);
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
	
	public static RFC822Name parse(String name){
		 String [] parts = name.split("@");
	     if (parts.length != 2) {
	            throw new IllegalArgumentException(
	            		String.format("Given value=\"%s\" is invalid RFC822 name", name));
	     }
	     return new RFC822Name(parts[0], parts[1]);
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
