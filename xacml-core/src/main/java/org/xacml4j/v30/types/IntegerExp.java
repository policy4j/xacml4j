package org.xacml4j.v30.types;


public final class IntegerExp
	extends BaseAttributeExp<Long>
{
	private static final long serialVersionUID = 6654857010399020496L;

	public IntegerExp(Long value) {
		super(IntegerType.INTEGER, value);
	}

	public IntegerExp add(IntegerExp d){
		return  new IntegerExp(getValue() + d.getValue());
	}

	public IntegerExp substract(IntegerExp d){
		return  new IntegerExp(getValue() - d.getValue());
	}

	public IntegerExp multiply(IntegerExp d){
		return  new IntegerExp(getValue() * d.getValue());
	}

	public IntegerExp divide(IntegerExp d){
		return  new IntegerExp(getValue() / d.getValue());
	}
}

