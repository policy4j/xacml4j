package org.xacml4j.v30.types;

import java.util.Collection;

import javax.xml.datatype.Duration;

import com.google.common.base.Preconditions;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.BagOfAttributeExpType;
import org.xacml4j.v30.YearMonthDuration;


public enum YearMonthDurationType implements AttributeExpType
{
	YEARMONTHDURATION("http://www.w3.org/2001/XMLSchema#yearMonthDuration");

	private final String typeId;
	private final BagOfAttributeExpType bagType;

	private YearMonthDurationType(String typeId)
	{
		this.typeId = typeId;
		this.bagType = new BagOfAttributeExpType(this);
	}

	public boolean isConvertibleFrom(Object any) {
		return any instanceof Duration || any instanceof String || any instanceof YearMonthDuration;
	}

	@Override
	public YearMonthDurationExp fromXacmlString(String v, Object ...params)
	{
		return new YearMonthDurationExp(this, YearMonthDuration.create(v));
	}

	@Override
	public YearMonthDurationExp create(Object any, Object ...params){
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertibleFrom(any),
				"Value=\"%s\" of type=\"%s\" can't be converted to XACML \"%s\" type",
				any, any.getClass(), typeId);
		return new YearMonthDurationExp(this, YearMonthDuration.create(any));
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



