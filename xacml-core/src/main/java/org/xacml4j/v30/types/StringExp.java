package org.xacml4j.v30.types;

import org.xacml4j.v30.BagOfAttributeExp;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public final class StringExp extends BaseAttributeExp<String>
{
	private static final long serialVersionUID = 657672949137533611L;

	private StringExp(String value) {
		super(XacmlTypes.STRING, value);
	}
	
	/**
	 * Creates {@link StringExp} from given string instance
	 * 
	 * @param v a string value
	 * @return {@link StringExp}
	 * @exception IllegalArgumentException if given
	 * string value is null or empty
	 */
	public static StringExp valueOf(String v){
		Preconditions.checkArgument(!Strings.isNullOrEmpty(v));
		return new StringExp(v);
	}
	
	/**
	 * Delegates to {@link XacmlTypes#STRING#emptyBag()}
	 * 
	 * @return {@link BagOfAttributeExp} empty bag
	 */
	public static BagOfAttributeExp emptyBag(){
		return XacmlTypes.STRING.emptyBag();
	}
	
	/**
	 * Delegates to {@link XacmlTypes#STRING#bag()}
	 * 
	 * @return {@link BagOfAttributeExp} empty bag
	 */
	public static BagOfAttributeExp.Builder bag(){
		return XacmlTypes.STRING.bag();
	}
	
	public boolean equalsIgnoreCase(StringExp v){
		return getValue().equalsIgnoreCase(v.getValue());
	}
	
	public StringExp concat(StringExp v){
		StringBuilder b = new StringBuilder(getValue());
		return StringExp.valueOf(b.append(v).toString());
	}
	
	public StringExp trim(){
		return StringExp.valueOf(getValue().trim());
	}
	
	public boolean startsWith(StringExp v){
		return  getValue().startsWith(v.getValue());
	}
	
	public boolean contains(StringExp v){
		return  getValue().contains(v.getValue());
	}
	
	public boolean endsWith(StringExp v){
		return  getValue().endsWith(v.getValue());
	}
	
	public StringExp toLowerCase(){
		return StringExp.valueOf(getValue().toLowerCase());
	}
	
	public StringExp toUpperCase(){
		return StringExp.valueOf(getValue().toUpperCase());
	}
}

