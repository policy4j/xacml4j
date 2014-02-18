package org.xacml4j.v30.types;

import org.oasis.xacml.v30.jaxb.AttributeValueType;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.BagOfAttributeExpType;
import org.xacml4j.v30.RFC822Name;
import org.xacml4j.v30.XacmlSyntaxException;

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
public enum RFC822NameType implements AttributeExpType, TypeToString, TypeToXacml30
{
	RFC822NAME("urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name");

	private final String typeId;
	private final BagOfAttributeExpType bagType;

	private RFC822NameType(String typeId){
		this.typeId = typeId;
		this.bagType = new BagOfAttributeExpType(this);
	}
	
	public RFC822NameExp create(Object v)
	{
		Preconditions.checkNotNull(v);
        return new RFC822NameExp(RFC822Name.parse(v));
	}

	@Override
	public AttributeValueType toXacml30(Types types, AttributeExp v) {
		AttributeValueType xacml = new AttributeValueType();
		xacml.setDataType(v.getType().getDataTypeId());
		xacml.getContent().add(toString(v));
		return xacml;
	}

	@Override
	public RFC822NameExp fromXacml30(Types types, AttributeValueType v) {
		if(v.getContent().size() > 0){
			return fromString((String)v.getContent().get(0));
		}
		throw new XacmlSyntaxException(
				"No content found for the attribute value");
	}

	@Override
	public String toString(AttributeExp exp) {
		RFC822Name v = (RFC822Name)exp.getValue();
		return v.toString();
	}

	@Override
	public RFC822NameExp fromString(String v) {
       return create(v);
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
