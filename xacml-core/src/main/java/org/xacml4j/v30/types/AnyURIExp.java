package org.xacml4j.v30.types;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.xacml4j.v30.BagOfAttributeExp;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * Represents an XACML expression of {@link XacmlTypes#ANYURI} type.
 * 
 * @author Giedrius Trumpickas
 *
 */
public final class AnyURIExp extends BaseAttributeExp<URI>
{
	private static final long serialVersionUID = -1279561638068756670L;
	
	private AnyURIExp(URI value){
		super(XacmlTypes.ANYURI, value);
	}
	
	/**
	 * Creates an XACML expression of {@link XacmlTypes#ANYURI} 
	 * from a given string
	 * 
	 * @param v a string value
	 * @return {@link AnyURIExp} instance
	 */
	public static AnyURIExp valueOf(String v){
		Preconditions.checkArgument(!Strings.isNullOrEmpty(v));
		return new AnyURIExp(URI.create(v).normalize());
	}
	
	public static AnyURIExp valueOf(StringExp v){
		return valueOf(v.getValue());
	}
	
	public static AnyURIExp valueOf(URL v){
		try {
			return new AnyURIExp(v.toURI());
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	public static AnyURIExp valueOf(URI v){
		Preconditions.checkNotNull(v);
		return new AnyURIExp(v);
	}
	
	public StringExp toStringExp(){
		return StringExp.valueOf(getValue().toString());
	}
	
	public static BagOfAttributeExp emptyBag(){
		return XacmlTypes.ANYURI.emptyBag();
	}
	
	public static BagOfAttributeExp.Builder bag(){
		return XacmlTypes.ANYURI.bag();
	}
	
	public AnyURIExp normalize(){
		return new AnyURIExp(getValue().normalize());
	}
}

