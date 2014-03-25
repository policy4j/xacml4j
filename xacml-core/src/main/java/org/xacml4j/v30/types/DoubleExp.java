package org.xacml4j.v30.types;

import org.xacml4j.v30.BagOfAttributeExp;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public final class DoubleExp extends BaseAttributeExp<Double>
{
	private static final long serialVersionUID = -3689668541615314228L;

	private DoubleExp(Double value) {
		super(XacmlTypes.DOUBLE, value);
	}
	
	public static DoubleExp valueOf(Number value){
		Preconditions.checkNotNull(value);
		return new DoubleExp(value.doubleValue());
	}
	
	public static DoubleExp valueOf(String v){
		Preconditions.checkArgument(!Strings.isNullOrEmpty(v));
		 if (v.endsWith("INF")) {
	            int infIndex = v.lastIndexOf("INF");
	            v = v.substring(0, infIndex) + "Infinity";
	     }
		 return new DoubleExp(Double.parseDouble(v));
	}
	
	public StringExp toStringExp(){
		return StringExp.valueOf(getValue().toString());
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
	
	public static BagOfAttributeExp emptyBag(){
		return XacmlTypes.DOUBLE.emptyBag();
	}
	
	public static BagOfAttributeExp.Builder bag(){
		return XacmlTypes.DOUBLE.bag();
	}
}

