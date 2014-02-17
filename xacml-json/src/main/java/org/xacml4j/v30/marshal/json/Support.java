package org.xacml4j.v30.marshal.json;

import org.xacml4j.v30.types.Types;

import com.google.common.base.Preconditions;

class Support 
{
	protected Types types;
	
	protected Support(Types types){
		Preconditions.checkNotNull(types);
		this.types = types;
		registerJsonCapabilities(types);
	}
	
	private void registerJsonCapabilities(Types types)
	{
		for(TypeToGSon c : TypeToGSon.JsonTypes.values()){
			if(types.getCapability(c.getType(), TypeToGSon.class) == null){
				types.addCapability(c.getType(), TypeToGSon.class, c);
			}
		}
	}
}
