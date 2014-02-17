package org.xacml4j.v30.types;

import javax.security.auth.x500.X500Principal;

import org.oasis.xacml.v30.jaxb.AttributeValueType;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.BagOfAttributeExpType;

import com.google.common.base.Preconditions;

/**
 * XACML DataType:  <b>urn:oasis:names:tc:xacml:1.0:data-type:x500Name</b>.
 * <br>The "urn:oasis:names:tc:xacml:1.0:data-type:x500Name"
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
public enum X500NameType implements AttributeExpType, TypeToString, TypeToXacml30
{
	X500NAME("urn:oasis:names:tc:xacml:1.0:data-type:x500Name");

	private final String typeId;
	private final BagOfAttributeExpType bagType;

	private X500NameType(String typeId){
		this.typeId = typeId;
		this.bagType = new BagOfAttributeExpType(this);
	}

	public boolean isConvertibleFrom(Object any) {
		return X500Principal.class.isInstance(any) || String.class.isInstance(any);
	}
	
	public X500NameExp create(Object any){
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertibleFrom(any),
				"Value=\"%s\" of type=\"%s\" can't be converted to XACML \"%s\" type",
				any, any.getClass(), typeId);
		if(String.class.isInstance(any)){
			return fromString((String)any);
		}
		return new X500NameExp((X500Principal)any);
	}
	

	@Override
	public X500NameExp fromString(String v) {
		Preconditions.checkNotNull(v);
		return new X500NameExp(new X500Principal(v));
	}
	
	@Override
	public AttributeValueType toXacml30(Types types, AttributeExp v) {
		AttributeValueType xacml = new AttributeValueType();
		xacml.setDataType(v.getType().getDataTypeId());
		xacml.getContent().add(toString(v));
		return xacml;
	}

	@Override
	public X500NameExp fromXacml30(Types types, AttributeValueType v) {
		Preconditions.checkArgument(v.getDataType().equals(getDataTypeId()));
		return create((String)v.getContent().get(0));
	}
	
	@Override
	public String toString(AttributeExp exp) {
		X500NameExp v = (X500NameExp)exp;
		return v.getValue().toString();
	}
	

	@Override
	public String getDataTypeId() {
		return typeId;
	}

	@Override
	public BagOfAttributeExpType bagType() {
		return bagType;
	}

	@Override
	public BagOfAttributeExp.Builder bag(){
		return new BagOfAttributeExp.Builder(this);
	}

	@Override
	public BagOfAttributeExp bagOf(AttributeExp... values) {
		return bagType.create(values);
	}

	@Override
	public BagOfAttributeExp bagOf(Iterable<AttributeExp> values) {
		return bagType.create(values);
	}

	@Override
	public BagOfAttributeExp emptyBag() {
		return bagType.createEmpty();
	}

	@Override
	public String toString(){
		return typeId;
	}
}
