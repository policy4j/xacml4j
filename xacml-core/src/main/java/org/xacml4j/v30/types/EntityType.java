package org.xacml4j.v30.types;

import javax.xml.bind.JAXBElement;

import org.oasis.xacml.v30.jaxb.AttributeType;
import org.oasis.xacml.v30.jaxb.AttributeValueType;
import org.oasis.xacml.v30.jaxb.ContentType;
import org.oasis.xacml.v30.jaxb.ObjectFactory;
import org.w3c.dom.Node;
import org.xacml4j.util.DOMUtil;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.BagOfAttributeExpType;
import org.xacml4j.v30.Entity;

import com.google.common.base.Preconditions;

public enum EntityType implements AttributeExpType, TypeToXacml30
{
	ENTITY("urn:oasis:names:tc:xacml:3.0:data type:entity");
	
	private final String typeId;
	private final BagOfAttributeExpType bagType;

	private final static ObjectFactory XACML30_FACTORY = new ObjectFactory();
			
	private EntityType(String typeId){
		this.typeId = typeId;
		this.bagType = new BagOfAttributeExpType(this);
	}

	public EntityExp create(Entity entity){
		return new EntityExp(entity);
	}
	public boolean isConvertibleFrom(Object any) {
		return Boolean.class.isInstance(any) || String.class.isInstance(any);
	}

	@Override
	public AttributeValueType toXacml30(Types types, AttributeExp v) {
		Preconditions.checkArgument(v.getType().equals(this));
		AttributeValueType xacml30 = new AttributeValueType();
		xacml30.setDataType(v.getType().getDataTypeId());
		Entity entity = ((EntityExp)v).getValue();
		ContentType content = new ContentType();
		content.getContent().add(DOMUtil.copyNode(entity.getContent()));
		xacml30.getContent().add(XACML30_FACTORY.createContent(content));
		for(Attribute a : entity.getAttributes()){
			xacml30.getContent().add(XACML30_FACTORY.createAttribute(toXacml30(types, a)));
		}
		return xacml30;
	}
	
	private AttributeType toXacml30(Types types, Attribute a)
	{	
		AttributeType xacml30 = new AttributeType();
		xacml30.setAttributeId(a.getAttributeId());
		xacml30.setIssuer(a.getIssuer());
		xacml30.setIncludeInResult(a.isIncludeInResult());
		for(AttributeExp v : a.getValues()){
			TypeToXacml30 toXacml30 = types.getCapability(v.getType(), TypeToXacml30.class);
			Preconditions.checkState(toXacml30 != null);
			xacml30.getAttributeValue().add(toXacml30.toXacml30(types, v));
		}
		return xacml30;
	}

		@Override
	public AttributeExp fromXacml30(Types types, AttributeValueType v) {
		Entity.Builder b = Entity.builder();
		if(v.getContent().size() > 0 && 
				v.getContent().get(0) instanceof JAXBElement){
			JAXBElement<?> e = (JAXBElement<?>)v.getContent().get(0);
			int start = 0;
			if(e.getValue() instanceof ContentType){
				ContentType c = (ContentType)e.getValue();
				if(c.getContent().size() > 0){
					b.content((Node)(c.getContent().get(0)));
					start++;
				}
			}
			for(int i = start;  i < v.getContent().size(); i++){
				JAXBElement<?> attr = (JAXBElement<?>)v.getContent().get(i);
				Preconditions.checkState(attr.getValue() instanceof AttributeType);
				AttributeType xacml30Attr = (AttributeType)attr.getValue();
				b.attribute(fromXacml30(types, xacml30Attr));
			}
		}
		return create(b.build());
	}
		
	private Attribute fromXacml30(Types types, AttributeType attr) {
		Attribute.Builder b = Attribute.builder(attr.getAttributeId());
		b.issuer(attr.getIssuer());
		b.includeInResult(attr.isIncludeInResult());
		for(AttributeValueType v : attr.getAttributeValue()){
			TypeToXacml30 toXacml30 = types.getCapability(v.getDataType(), TypeToXacml30.class);
			Preconditions.checkState(toXacml30 != null);
			b.value(toXacml30.fromXacml30(types, v));
		}
		return b.build();
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
	public BagOfAttributeExp bagOf(AttributeExp... values) {
		return bagType.create(values);
	}

	@Override
	public BagOfAttributeExp.Builder bag(){
		return new BagOfAttributeExp.Builder(this);
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
