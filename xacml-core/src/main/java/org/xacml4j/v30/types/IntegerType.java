package org.xacml4j.v30.types;

import java.util.Collection;

import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.BagOfAttributeExpType;

import com.google.common.base.Preconditions;

public enum IntegerType implements AttributeExpType
{
	INTEGER("http://www.w3.org/2001/XMLSchema#integer");

	private final String typeId;
	private final BagOfAttributeExpType bagType;

	private IntegerType(String typeId){
		this.typeId = typeId;
		this.bagType = new BagOfAttributeExpType(this);
	}

	private boolean isConvertibleFrom(Object any) {
		return Long.class.isInstance(any) || Integer.class.isInstance(any) ||
		Short.class.isInstance(any) || Byte.class.isInstance(any) ||
		String.class.isInstance(any);
	}


	@Override
	public IntegerExp create(Object any, Object ...params){
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertibleFrom(any),
				"Value=\"%s\" of type=\"%s\" can't be converted to XACML \"%s\" type",
				any, any.getClass(), typeId);
		if(String.class.isInstance(any)){
			return fromXacmlString((String)any);
		}
		if(Byte.class.isInstance(any)){
			return new IntegerExp(this, ((Byte)any).longValue());
		}
		if(Short.class.isInstance(any)){
			return new IntegerExp(this, ((Short)any).longValue());
		}
		if(Integer.class.isInstance(any)){
			return new IntegerExp(this, ((Integer)any).longValue());
		}
		return new IntegerExp(this, (Long)any);
	}

	@Override
	public IntegerExp fromXacmlString(String v, Object ...params) {
        Preconditions.checkNotNull(v);
		if ((v.length() >= 1) &&
        		(v.charAt(0) == '+')){
			v = v.substring(1);
		}
		return new IntegerExp(this, Long.valueOf(v));
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
	public BagOfAttributeExp bagOf(Collection<AttributeExp> values) {
		return bagType.create(values);
	}

	@Override
	public BagOfAttributeExp.Builder bag(){
		return new BagOfAttributeExp.Builder(this);
	}

	@Override
	public BagOfAttributeExp bagOf(Object... values) {
		return bagType.bagOf(values);
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
