package org.xacml4j.v30.types;

import java.util.Collection;
import java.util.GregorianCalendar;

import javax.xml.datatype.XMLGregorianCalendar;

import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.BagOfAttributeExpType;
import org.xacml4j.v30.DateTime;


public enum DateTimeType implements AttributeExpType
{
	DATETIME("http://www.w3.org/2001/XMLSchema#dateTime");

	private String typeId;
	private BagOfAttributeExpType bagType;

	private DateTimeType(String typeId) {
		this.typeId = typeId;
		this.bagType = new BagOfAttributeExpType(this);
	}

	public boolean isConvertableFrom(Object any) {
		return XMLGregorianCalendar.class.isInstance(any)
				|| String.class.isInstance(any)
				|| GregorianCalendar.class.isInstance(any);
	}

	@Override
	public DateTimeExp fromXacmlString(String v, Object... params) {
		return new DateTimeExp(this, DateTime.create(v));
	}

	@Override
	public DateTimeExp create(Object any, Object... params) {
		return new DateTimeExp(this, DateTime.create(any));
	}

	@Override
	public String getDataTypeId() {
		return typeId;
	}

	public BagOfAttributeExp.Builder bag(){
		return new BagOfAttributeExp.Builder(this);
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