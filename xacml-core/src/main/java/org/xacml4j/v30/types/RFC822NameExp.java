package org.xacml4j.v30.types;

import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.RFC822Name;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public final class RFC822NameExp extends BaseAttributeExp<RFC822Name>
{
	private static final long serialVersionUID = -1983511364298319436L;

	RFC822NameExp(RFC822Name value) {
		super(XacmlTypes.RFC822NAME, value);
	}
	
	public static RFC822NameExp valueOf(String v){
		Preconditions.checkArgument(!Strings.isNullOrEmpty(v));
        return new RFC822NameExp(RFC822Name.parse(v));
	}
	
	public static RFC822NameExp valueOf(StringExp v){
		return valueOf(v.getValue());
	}
	
	public static RFC822NameExp valueOf(RFC822Name n){
		return new RFC822NameExp(n);
	}
	
	public StringExp toStringExp(){
		return StringExp.valueOf(getValue().toXacmlString());
	}
	
	public static BagOfAttributeExp emptyBag(){
		return XacmlTypes.RFC822NAME.emptyBag();
	}
	
	public static BagOfAttributeExp.Builder bag(){
		return XacmlTypes.RFC822NAME.bag();
	}
}
