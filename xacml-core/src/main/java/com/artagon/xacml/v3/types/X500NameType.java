package com.artagon.xacml.v3.types;

import java.util.Collection;

import javax.security.auth.x500.X500Principal;

import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.BagOfAttributeValuesType;
import com.google.common.base.Preconditions;

/** 
 * XACML DataType:  <b>urn:oasis:names:tc:xacml:1.0:data-type:x500Name</b>.
 * <br>The “urn:oasis:names:tc:xacml:1.0:data-type:x500Name” 
 * primitive type represents an ITU-T Rec. X.520 Distinguished 
 * Name. 
 * <br>The valid syntax for such a name is described in IETF 
 * RFC 2253 "Lightweight Directory Access Protocol (v3): 
 * UTF-8 String Representation of Distinguished Names"
 * <p>
 * For example the general nature of this syntax is described in
 * the constructor for javax.security.auth.x500.X500Principal(String name): Creates an X500Principal 
 * from a string representation of an X.500 distinguished name 
 * <br>(ex: "CN=Duke, OU=JavaSoft, O=Sun Microsystems, C=US"). 
 * <br>The distinguished name must be specified using the grammar defined 
 * in RFC 1779 or RFC 2253 (either format is acceptable). 
 */
public enum X500NameType implements AttributeValueType
{
	X500NAME("urn:oasis:names:tc:xacml:1.0:data-type:x500Name");
	
	private String typeId;
	private BagOfAttributeValuesType bagType;
	
	private X500NameType(String typeId){
		this.typeId = typeId;
		this.bagType = new BagOfAttributeValuesType(this);
	}
	
	@Override
	public boolean isConvertableFrom(Object any) {
		return X500Principal.class.isInstance(any) || String.class.isInstance(any);
	}
	
	@Override
	public X500NameValue fromXacmlString(String v, Object ...params) {
		Preconditions.checkNotNull(v);
		return new X500NameValue(this, new X500Principal(v));
	}
	
	@Override
	public X500NameValue create(Object any, Object ...params){
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertableFrom(any), String.format(
				"Value=\"%s\" of class=\"%s\" can't ne converted to XACML \"x500Name\" type", 
				any, any.getClass()));
		if(String.class.isInstance(any)){
			return fromXacmlString((String)any);
		}
		return new X500NameValue(this, (X500Principal)any);
	}
	@Override
	public String getDataTypeId() {
		return typeId;
	}

	@Override
	public BagOfAttributeValuesType bagType() {
		return bagType;
	}

	@Override
	public BagOfAttributeValues bagOf(AttributeValue... values) {
		return bagType.create(values);
	}

	@Override
	public BagOfAttributeValues bagOf(Collection<AttributeValue> values) {
		return bagType.create(values);
	}

	@Override
	public BagOfAttributeValues emptyBag() {
		return bagType.createEmpty();
	}
	
	@Override
	public String toString(){
		return typeId;
	}
}