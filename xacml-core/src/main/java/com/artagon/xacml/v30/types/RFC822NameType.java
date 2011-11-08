package com.artagon.xacml.v30.types;

import java.util.Collection;

import com.artagon.xacml.v30.AttributeExp;
import com.artagon.xacml.v30.AttributeExpType;
import com.artagon.xacml.v30.BagOfAttributesExp;
import com.artagon.xacml.v30.BagOfAttributesExpType;
import com.artagon.xacml.v30.core.RFC822Name;
import com.google.common.base.Preconditions;

/** 
 * XACML DataType:  <b>urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name</b>. 
 * <br>The "urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name" primitive type 
 * represents an electronic mail address. The valid syntax for such a 
 * name is described in IETF RFC 2821, Section 4.1.2, Command Argument 
 * Syntax, under the term "Mailbox". 
 * From rfc 2821:
 * <pre>
 *   Domain = (sub-domain 1*("." sub-domain)) / address-literal
 *   sub-domain = Let-dig [Ldh-str]
 *   address-literal = "[" IPv4-address-literal /
 *                         IPv6-address-literal /
 *                         General-address-literal "]"
 *   Mailbox = Local-part "@" Domain
 *   Local-part = Dot-string / Quoted-string
 *   Dot-string = Atom *("." Atom)
 *   Atom = 1*atext
 *   Quoted-string = DQUOTE *qcontent DQUOTE
 *   String = Atom / Quoted-string
 * </pre>
 * */
public enum RFC822NameType implements AttributeExpType
{	
	RFC822NAME("urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name");
	
	private String typeId;
	private BagOfAttributesExpType bagType;
	
	private RFC822NameType(String typeId){
		this.typeId = typeId;
		this.bagType = new BagOfAttributesExpType(this);
	}
	
	public boolean isConvertableFrom(Object any) {
		return String.class.isInstance(any) || RFC822Name.class.isInstance(any);
	}
	
	@Override
	public RFC822NameValueExp fromXacmlString(String v, Object ...params)
	{
		Preconditions.checkNotNull(v);
        return new RFC822NameValueExp(this, RFC822Name.parse(v));
	}
	
	@Override
	public RFC822NameValueExp create(Object any, Object ...params)
	{
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertableFrom(any),String.format(
				"Value=\"%s\" of class=\"%s\" can't ne converted to XACML \"rfc822Name\" type", 
				any, any.getClass()));
		if(any instanceof String){
			return fromXacmlString((String)any);
		}
		return new RFC822NameValueExp(this, (RFC822Name)any);
	}
	
	@Override
	public String getDataTypeId() {
		return typeId;
	}

	@Override
	public BagOfAttributesExpType bagType() {
		return bagType;
	}

	@Override
	public BagOfAttributesExp bagOf(AttributeExp... values) {
		return bagType.create(values);
	}

	@Override
	public BagOfAttributesExp bagOf(Collection<AttributeExp> values) {
		return bagType.create(values);
	}

	@Override
	public BagOfAttributesExp bagOf(Object... values) {
		return bagType.bagOf(values);
	}
	
	@Override
	public BagOfAttributesExp emptyBag() {
		return bagType.createEmpty();
	}
	
	@Override
	public String toString(){
		return typeId;
	}
}