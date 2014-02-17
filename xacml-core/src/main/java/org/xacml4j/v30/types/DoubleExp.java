package org.xacml4j.v30.types;


public final class DoubleExp extends BaseAttributeExp<Double>
{
	private static final long serialVersionUID = -3689668541615314228L;

	public DoubleExp(Double value) {
		super(DoubleType.DOUBLE, value);
	}

	public DoubleExp add(DoubleExp d){
		return  new DoubleExp(getValue() + d.getValue());
	}

	public DoubleExp substract(DoubleExp d){
		return  new DoubleExp(getValue() - d.getValue());
	}

	public DoubleExp multiply(DoubleExp d){
		return  new DoubleExp(getValue() * d.getValue());
	}

	public DoubleExp divide(DoubleExp d){
		return  new DoubleExp(getValue() / d.getValue());
	}
}

