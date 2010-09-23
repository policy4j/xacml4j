package com.artagon.xacml.v3;

public interface BagOfAttributeValuesI extends ValueExpression
{
	boolean isEmpty();
	boolean contains(ValueExpression v);
	int count(ValueExpression v);
	BagOfAttributeValuesI union(BagOfAttributeValuesI bag);
	BagOfAttributeValuesI intersection(BagOfAttributeValuesI bag);
}
