package com.artagon.xacml.v3;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class RequestReference extends XacmlObject
{
	private Collection<AttributesReference> references;
	
	public RequestReference(Collection<AttributesReference> references){
		this.references = new LinkedList<AttributesReference>(references);
	}
	
	public Collection<AttributesReference> getReferencedAttributes(){
		return Collections.unmodifiableCollection(references);
	}
}
